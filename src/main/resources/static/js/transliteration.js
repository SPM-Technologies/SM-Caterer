/**
 * SM-Caterer Transliteration Module
 * Uses Google Input Tools API for accurate Hindi/Marathi transliteration.
 *
 * Features:
 *  - When page language is 'hi' or 'mr', transliteration is ON by default.
 *  - Shows suggestion dropdown with multiple candidates (like Google Input Tools).
 *  - Arrow keys / number keys to pick a suggestion; Enter/Space to accept top pick.
 *  - Ctrl+Space toggles transliteration per-field; navbar button toggles globally.
 *  - Auto-excludes fields that should stay English (email, password, codes, etc.).
 */
(function () {
    'use strict';

    // ===== Configuration =====
    // Uses server-side proxy to avoid CORS issues with Google API
    var PROXY_API = '/api/v1/transliterate';
    var MAX_SUGGESTIONS = 8;
    var DEBOUNCE_MS = 200;

    // Fields that should NEVER be transliterated
    var EXCLUDED_TYPES = [
        'email', 'password', 'number', 'date', 'datetime-local',
        'time', 'url', 'tel', 'color', 'range', 'hidden'
    ];

    // IDs/names that must stay English (partial match, case-insensitive)
    var EXCLUDED_PATTERNS = [
        'username', 'email', 'password', 'rawpassword',
        'code', 'Code',
        'gstin', 'pincode', 'phone',
        'upiId', 'upi_id', 'defaultUpiId',
        'smtpHost', 'smtpPort', 'smtpUsername', 'smtpPassword', 'smtpFromEmail',
        'testEmailAddress',
        'primaryColor', 'colorPicker',
        'csrf', '_csrf',
        'transactionReference',
        'orderNumber', 'paymentNumber'
    ];

    // ===== State =====
    var currentLang = 'en';
    var globalEnabled = false;
    var perFieldOverride = {};   // id -> true/false
    var activeField = null;      // currently focused input
    var currentWord = '';        // English word being typed
    var wordStart = -1;          // position where current word starts
    var suggestions = [];        // current suggestion list
    var selectedIndex = 0;       // highlighted suggestion
    var debounceTimer = null;
    var dropdown = null;         // suggestion dropdown element
    var requestId = 0;           // to discard stale API responses

    // ===== Suggestion Dropdown =====

    function createDropdown() {
        if (dropdown) return dropdown;
        dropdown = document.createElement('div');
        dropdown.id = 'translit-suggestions';
        dropdown.style.cssText = [
            'position:fixed', 'z-index:99999', 'background:#fff',
            'border:1px solid #ccc', 'border-radius:6px',
            'box-shadow:0 4px 16px rgba(0,0,0,.18)', 'max-width:360px',
            'min-width:120px', 'display:none', 'font-size:15px',
            'overflow:hidden'
        ].join(';');
        document.body.appendChild(dropdown);
        return dropdown;
    }

    function showDropdown(items, field) {
        if (!items || items.length === 0) { hideDropdown(); return; }

        suggestions = items;
        selectedIndex = 0;
        var dd = createDropdown();
        dd.innerHTML = '';

        items.forEach(function (text, idx) {
            var div = document.createElement('div');
            div.textContent = (idx + 1) + '. ' + text;
            div.style.cssText = 'padding:6px 14px;cursor:pointer;white-space:nowrap;';
            div.dataset.idx = idx;
            if (idx === 0) div.style.background = '#e8f0fe';

            div.addEventListener('mousedown', function (e) {
                e.preventDefault();  // don't blur the input
                acceptSuggestion(parseInt(this.dataset.idx, 10));
            });
            div.addEventListener('mouseenter', function () {
                highlightItem(parseInt(this.dataset.idx, 10));
            });
            dd.appendChild(div);
        });

        // Position below the caret
        positionDropdown(field);
        dd.style.display = 'block';
    }

    function hideDropdown() {
        if (dropdown) dropdown.style.display = 'none';
        suggestions = [];
        selectedIndex = 0;
        currentWord = '';
        wordStart = -1;
    }

    function highlightItem(idx) {
        if (!dropdown) return;
        var children = dropdown.children;
        for (var i = 0; i < children.length; i++) {
            children[i].style.background = (i === idx) ? '#e8f0fe' : '#fff';
        }
        selectedIndex = idx;
    }

    function positionDropdown(field) {
        if (!dropdown || !field) return;
        var rect = field.getBoundingClientRect();
        var top = rect.bottom + 4;
        var left = rect.left;

        // If dropdown goes off-screen bottom, show above
        if (top + 200 > window.innerHeight) {
            top = rect.top - 200;
        }
        // If dropdown goes off-screen right
        if (left + 300 > window.innerWidth) {
            left = window.innerWidth - 310;
        }

        dropdown.style.top = top + 'px';
        dropdown.style.left = left + 'px';
    }

    // ===== Accept suggestion =====

    function acceptSuggestion(idx) {
        if (idx < 0 || idx >= suggestions.length) { hideDropdown(); return; }

        var field = activeField;
        if (!field) { hideDropdown(); return; }

        var val = field.value;
        var chosenText = suggestions[idx];
        var before = val.substring(0, wordStart);
        var after = val.substring(wordStart + currentWord.length);
        var newVal = before + chosenText + after;
        var newPos = before.length + chosenText.length;

        field.value = newVal;
        field.setSelectionRange(newPos, newPos);
        field.dispatchEvent(new Event('input', { bubbles: true }));
        field.dispatchEvent(new Event('change', { bubbles: true }));

        hideDropdown();
        field.focus();
    }

    // ===== Transliteration API (via server proxy) =====

    function fetchSuggestions(word, callback) {
        if (!word || !currentLang) { callback([]); return; }

        requestId++;
        var myReqId = requestId;

        var url = PROXY_API + '?text=' + encodeURIComponent(word) + '&lang=' + currentLang;

        var controller = new AbortController();
        var timer = setTimeout(function () { controller.abort(); }, 3000);

        fetch(url, { signal: controller.signal })
            .then(function (resp) { return resp.json(); })
            .then(function (data) {
                clearTimeout(timer);
                if (myReqId !== requestId) return; // stale
                // Response format: ["SUCCESS", [["word", ["suggestion1","suggestion2",...]]]]
                if (data && data[0] === 'SUCCESS' && data[1] && data[1][0] && data[1][0][1]) {
                    callback(data[1][0][1]);
                } else {
                    callback([]);
                }
            })
            .catch(function () {
                clearTimeout(timer);
                if (myReqId !== requestId) return;
                callback([]);
            });
    }

    // ===== Field detection =====

    function shouldExcludeField(el) {
        if (el.classList.contains('no-transliterate')) return true;

        var type = (el.getAttribute('type') || 'text').toLowerCase();
        if (EXCLUDED_TYPES.indexOf(type) !== -1) return true;

        var id = (el.id || '').toLowerCase();
        var name = (el.name || '').toLowerCase();
        for (var p = 0; p < EXCLUDED_PATTERNS.length; p++) {
            var pat = EXCLUDED_PATTERNS[p].toLowerCase();
            if (id.indexOf(pat) !== -1 || name.indexOf(pat) !== -1) return true;
        }
        return false;
    }

    function isTransliterableField(el) {
        if (el.classList.contains('transliterable') || el.getAttribute('data-transliterate') === 'true') {
            return true;
        }
        var tag = el.tagName.toLowerCase();
        if (tag === 'textarea') return !shouldExcludeField(el);
        if (tag === 'input') {
            var type = (el.getAttribute('type') || 'text').toLowerCase();
            if (type === 'text' || type === 'search') return !shouldExcludeField(el);
        }
        return false;
    }

    function isEnabledForField(el) {
        if (!globalEnabled) return false;
        var fid = el.id || el.name || '';
        if (fid && perFieldOverride[fid] !== undefined) {
            return perFieldOverride[fid];
        }
        return isTransliterableField(el);
    }

    // ===== Input handling =====

    function onInput(e) {
        var el = e.target;
        if (!isEnabledForField(el)) return;

        activeField = el;

        var pos = el.selectionStart;
        if (pos === undefined || pos === null) return;

        var val = el.value;
        var before = val.substring(0, pos);

        // Find the current English word being typed (Latin chars at end of before-cursor text)
        var match = before.match(/([a-zA-Z]+)$/);
        if (!match) {
            hideDropdown();
            return;
        }

        currentWord = match[1];
        wordStart = before.length - currentWord.length;

        // Debounce API calls
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function () {
            fetchSuggestions(currentWord, function (items) {
                if (items.length > 0) {
                    showDropdown(items, el);
                } else {
                    hideDropdown();
                }
            });
        }, DEBOUNCE_MS);
    }

    function onKeyDown(e) {
        var el = e.target;

        // Ctrl+Space: toggle transliteration for this field
        if (e.ctrlKey && e.code === 'Space') {
            e.preventDefault();
            var fid = el.id || el.name || '';
            if (!fid) return;
            var on = isEnabledForField(el);
            perFieldOverride[fid] = !on;
            updateFieldVisual(el);
            if (on) hideDropdown();  // turning off
            return;
        }

        // If dropdown is not visible, skip
        if (!dropdown || dropdown.style.display === 'none' || suggestions.length === 0) return;

        // Number keys 1-9 to pick suggestion
        if (e.key >= '1' && e.key <= '9') {
            var numIdx = parseInt(e.key, 10) - 1;
            if (numIdx < suggestions.length) {
                e.preventDefault();
                acceptSuggestion(numIdx);
                return;
            }
        }

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                highlightItem(Math.min(selectedIndex + 1, suggestions.length - 1));
                break;
            case 'ArrowUp':
                e.preventDefault();
                highlightItem(Math.max(selectedIndex - 1, 0));
                break;
            case 'Enter':
                e.preventDefault();
                acceptSuggestion(selectedIndex);
                break;
            case 'Tab':
                // Accept top suggestion on Tab
                e.preventDefault();
                acceptSuggestion(selectedIndex);
                break;
            case ' ':
                // Space accepts the top suggestion and adds a space
                e.preventDefault();
                acceptSuggestion(selectedIndex);
                // Insert space after accepted word
                if (activeField) {
                    var p = activeField.selectionStart;
                    var v = activeField.value;
                    activeField.value = v.substring(0, p) + ' ' + v.substring(p);
                    activeField.setSelectionRange(p + 1, p + 1);
                }
                break;
            case 'Escape':
                hideDropdown();
                break;
        }
    }

    function onBlur(e) {
        // Delay hiding to allow mousedown on dropdown
        setTimeout(function () {
            if (dropdown && dropdown.style.display !== 'none') {
                // Auto-accept top suggestion on blur if there are suggestions
                if (suggestions.length > 0 && activeField === e.target) {
                    acceptSuggestion(0);
                }
                hideDropdown();
            }
        }, 200);
    }

    // ===== Visual indicator =====

    function updateFieldVisual(el) {
        var on = isEnabledForField(el);
        if (on) {
            el.style.borderColor = '#fd7e14';
            el.style.boxShadow = '0 0 0 0.15rem rgba(253,126,20,0.2)';
        } else {
            el.style.borderColor = '';
            el.style.boxShadow = '';
        }
    }

    function refreshAllFields() {
        var inputs = document.querySelectorAll('input[type="text"], input[type="search"], input:not([type]), textarea');
        inputs.forEach(function (el) { updateFieldVisual(el); });
    }

    // ===== Navbar toggle =====

    function createNavToggle() {
        if (document.getElementById('translitToggleNav')) return;

        var langIcon = document.querySelector('.navbar .bi-globe');
        if (!langIcon) return;

        var navItem = document.createElement('li');
        navItem.className = 'nav-item d-flex align-items-center me-2';

        var btn = document.createElement('button');
        btn.type = 'button';
        btn.id = 'translitToggleNav';
        btn.className = 'btn btn-sm';
        btn.style.cssText = 'cursor:pointer;padding:4px 10px;border-radius:20px;font-size:13px;';
        btn.title = 'Toggle Devanagari typing (Ctrl+Space per field)';

        function updateBtn() {
            if (globalEnabled) {
                btn.className = 'btn btn-sm btn-warning';
                btn.innerHTML = '<i class="bi bi-keyboard-fill"></i> <strong>अ</strong>';
            } else {
                btn.className = 'btn btn-sm btn-outline-light';
                btn.innerHTML = '<i class="bi bi-keyboard"></i> A';
            }
        }
        updateBtn();

        btn.addEventListener('click', function () {
            globalEnabled = !globalEnabled;
            localStorage.setItem('sm_translit_enabled', globalEnabled ? '1' : '0');
            updateBtn();
            refreshAllFields();
            if (!globalEnabled) hideDropdown();
        });

        navItem.appendChild(btn);

        var langLi = langIcon.closest('.nav-item');
        if (langLi && langLi.parentNode) {
            langLi.parentNode.insertBefore(navItem, langLi);
        }
    }

    // ===== Init =====

    function init() {
        currentLang = document.documentElement.lang || 'en';

        if (currentLang !== 'hi' && currentLang !== 'mr') {
            globalEnabled = false;
            return;
        }

        // Default ON; respect saved preference
        var saved = localStorage.getItem('sm_translit_enabled');
        globalEnabled = (saved !== '0');  // ON unless explicitly disabled

        // Event delegation
        document.addEventListener('input', onInput, true);
        document.addEventListener('keydown', onKeyDown, true);
        document.addEventListener('focusout', onBlur, true);

        // Close dropdown on click outside
        document.addEventListener('click', function (e) {
            if (dropdown && !dropdown.contains(e.target)) {
                hideDropdown();
            }
        });

        // Close dropdown on scroll/resize
        window.addEventListener('scroll', hideDropdown, true);
        window.addEventListener('resize', hideDropdown);

        createNavToggle();
        refreshAllFields();

        // Watch for dynamically added fields
        new MutationObserver(refreshAllFields)
            .observe(document.body, { childList: true, subtree: true });
    }

    // ===== Public API =====
    window.Transliteration = {
        init: init,
        isEnabled: function () { return globalEnabled; },
        enable: function () { globalEnabled = true; refreshAllFields(); },
        disable: function () { globalEnabled = false; hideDropdown(); refreshAllFields(); },
        toggle: function () { globalEnabled = !globalEnabled; refreshAllFields(); }
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
