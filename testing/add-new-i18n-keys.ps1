# Add new i18n keys needed for hardcoded template conversion
# These keys are missing from all 3 message files

$basePath = "D:\Projects\AI\Caterer\SM-Caterer\src\main\resources\messages"

# =====================================================
# ENGLISH - messages.properties
# =====================================================
$enKeys = @"

# =====================================================
# Additional Keys for Template i18n Conversion
# =====================================================

# Common
common.yes=Yes
common.no=No
common.send=Send
common.close=Close
common.breadcrumb.admin=Admin
common.view=View

# Admin Navigation (for main layout)
admin.nav.dashboard=Dashboard
admin.nav.tenants=Tenants
admin.nav.users=Users

# Admin Dashboard (hardcoded strings)
admin.dashboard.noTenants=No tenants found
admin.dashboard.code=Code
admin.dashboard.businessName=Business Name
admin.dashboard.contact=Contact
admin.dashboard.status=Status
admin.dashboard.subscriptionEnd=Subscription End

# Admin Users Form (hardcoded strings)
admin.users.fullName=Full Name
admin.users.passwordNew=Password *
admin.users.breadcrumb.add=Add
admin.users.breadcrumb.edit=Edit
admin.users.lang.english=English
admin.users.lang.hindi=Hindi
admin.users.lang.marathi=Marathi

# Payments (hardcoded strings)
payments.orderDetails=Order Details
payments.orderNumber=Order Number
payments.customer=Customer
payments.orderTotal=Order Total
payments.balanceDue=Balance Due
payments.maximum=Maximum
payments.transRefHint=Optional: UTR number, cheque number, etc.
payments.upiIdHint=Optional: Payer's UPI ID for UPI payments
payments.resendEmail=Resend Email
payments.emailSent=Email Sent
payments.viewOrder=View Order
payments.viewAllPayments=View All Payments for Order

# Orders Print
orders.print.printOrder=Print Order
orders.print.close=Close
orders.print.order=ORDER
orders.print.customer=Customer
orders.print.eventDetails=Event Details
orders.print.guests=Guests
orders.print.venue=Venue
orders.print.menuItems=Menu Items
orders.print.item=Item
orders.print.qty=Qty
orders.print.pricePerItem=Price/Item
orders.print.pricePerUnit=Price/Unit
orders.print.subtotal=Subtotal
orders.print.menuSubtotal=Menu Subtotal
orders.print.utilities=Utilities
orders.print.utilitySubtotal=Utility Subtotal
orders.print.discount=Discount
orders.print.tax=Tax
orders.print.grandTotal=Grand Total
orders.print.paid=Paid
orders.print.balanceDue=Balance Due
orders.print.notes=Notes
orders.print.thankYou=Thank you for your business!
orders.print.generatedOn=Generated on

# Email Settings (hardcoded strings)
email.settings.configured=Email is configured
email.settings.notConfigured=Email is not configured
email.settings.configuredHint=Your email notifications are ready to send.
email.settings.notConfiguredHint=Configure SMTP settings to enable email notifications.
email.settings.smtpConfig=SMTP Configuration
email.settings.smtp.hostHint=e.g., smtp.gmail.com, smtp.sendgrid.net
email.settings.smtp.portHint=Usually 587 (TLS) or 465 (SSL)
email.settings.smtp.passwordHint=For Gmail, use an App Password
email.settings.sendTest=Send Test Email
email.settings.cancel=Cancel
email.settings.sending=Sending...
email.settings.enterEmail=Please enter an email address
email.settings.sendError=Error sending test email

# Payment Settings (hardcoded strings)
payment.settings.title=Payment Settings
payment.settings.enabled=Payment feature is enabled
payment.settings.disabled=Payment feature is disabled
payment.settings.enabledHint=You can record and track payments.
payment.settings.disabledHint=Enable to start recording payments.
payment.settings.enableFeature=Enable Payment Feature
payment.settings.disabledNote=When disabled, payment recording will not be available.
payment.settings.upiTitle=UPI QR Code Settings
payment.settings.upiDescription=Configure default UPI details for generating payment QR codes.
payment.settings.defaultUpiId=Default UPI ID
payment.settings.upiIdHint=Your UPI Virtual Payment Address (VPA)
payment.settings.upiPayeeName=UPI Payee Name
payment.settings.upiPayeeHint=Name shown to payer in UPI apps
payment.settings.upiReady=UPI QR code generation is ready.
payment.settings.upiNotReady=Fill in both UPI ID and Payee Name to enable QR code generation.

# Customers (hardcoded JS confirm)
customers.confirmDelete=Are you sure you want to delete this customer?

# Footer
footer.rights=All rights reserved.
footer.adminPortal=SM-Caterer Admin Portal
"@

# =====================================================
# MARATHI - messages_mr.properties
# =====================================================
$mrKeys = @"

# =====================================================
# Additional Keys for Template i18n Conversion
# =====================================================

# Common
common.yes=\u0939\u094B\u092F
common.no=\u0928\u093E\u0939\u0940
common.send=\u092A\u093E\u0920\u0935\u093E
common.close=\u092C\u0902\u0926 \u0915\u0930\u093E
common.breadcrumb.admin=\u0905\u0945\u0921\u092E\u093F\u0928
common.view=\u092A\u0939\u093E

# Admin Navigation (for main layout)
admin.nav.dashboard=\u0921\u0945\u0936\u092C\u094B\u0930\u094D\u0921
admin.nav.tenants=\u091F\u0947\u0928\u0902\u091F
admin.nav.users=\u0935\u093E\u092A\u0930\u0915\u0930\u094D\u0924\u0947

# Admin Dashboard (hardcoded strings)
admin.dashboard.noTenants=\u091F\u0947\u0928\u0902\u091F \u0938\u093E\u092A\u0921\u0932\u0947 \u0928\u093E\u0939\u0940\u0924
admin.dashboard.code=\u0915\u094B\u0921
admin.dashboard.businessName=\u0935\u094D\u092F\u0935\u0938\u093E\u092F \u0928\u093E\u0935
admin.dashboard.contact=\u0938\u0902\u092A\u0930\u094D\u0915
admin.dashboard.status=\u0938\u094D\u0925\u093F\u0924\u0940
admin.dashboard.subscriptionEnd=\u0938\u0926\u0938\u094D\u092F\u0924\u094D\u0935 \u0938\u0902\u092A\u0941\u0937\u094D\u091F\u0940

# Admin Users Form (hardcoded strings)
admin.users.fullName=\u092A\u0942\u0930\u094D\u0923 \u0928\u093E\u0935
admin.users.passwordNew=\u092A\u093E\u0938\u0935\u0930\u094D\u0921 *
admin.users.breadcrumb.add=\u091C\u094B\u0921\u093E
admin.users.breadcrumb.edit=\u0938\u0902\u092A\u093E\u0926\u093F\u0924 \u0915\u0930\u093E
admin.users.lang.english=English
admin.users.lang.hindi=\u0939\u093F\u0902\u0926\u0940
admin.users.lang.marathi=\u092E\u0930\u093E\u0920\u0940

# Payments (hardcoded strings)
payments.orderDetails=\u0911\u0930\u094D\u0921\u0930 \u0924\u092A\u0936\u0940\u0932
payments.orderNumber=\u0911\u0930\u094D\u0921\u0930 \u0915\u094D\u0930\u092E\u093E\u0902\u0915
payments.customer=\u0917\u094D\u0930\u093E\u0939\u0915
payments.orderTotal=\u0911\u0930\u094D\u0921\u0930 \u090F\u0915\u0942\u0923
payments.balanceDue=\u0936\u093F\u0932\u094D\u0932\u0915 \u0930\u0915\u094D\u0915\u092E
payments.maximum=\u0915\u092E\u093E\u0932
payments.transRefHint=\u0910\u091A\u094D\u091B\u093F\u0915: UTR \u0915\u094D\u0930\u092E\u093E\u0902\u0915, \u091A\u0947\u0915 \u0915\u094D\u0930\u092E\u093E\u0902\u0915, \u0907.
payments.upiIdHint=\u0910\u091A\u094D\u091B\u093F\u0915: UPI \u092A\u0947\u092E\u0947\u0902\u091F\u0938\u093E\u0920\u0940 \u092A\u0947\u092F\u0930\u091A\u093E UPI ID
payments.resendEmail=\u0908\u092E\u0947\u0932 \u092A\u0941\u0928\u094D\u0939\u093E \u092A\u093E\u0920\u0935\u093E
payments.emailSent=\u0908\u092E\u0947\u0932 \u092A\u093E\u0920\u0935\u0932\u0947\u0932\u0947
payments.viewOrder=\u0911\u0930\u094D\u0921\u0930 \u092A\u0939\u093E
payments.viewAllPayments=\u0911\u0930\u094D\u0921\u0930\u0938\u093E\u0920\u0940 \u0938\u0930\u094D\u0935 \u092A\u0947\u092E\u0947\u0902\u091F \u092A\u0939\u093E

# Orders Print
orders.print.printOrder=\u0911\u0930\u094D\u0921\u0930 \u092A\u094D\u0930\u093F\u0902\u091F \u0915\u0930\u093E
orders.print.close=\u092C\u0902\u0926 \u0915\u0930\u093E
orders.print.order=\u0911\u0930\u094D\u0921\u0930
orders.print.customer=\u0917\u094D\u0930\u093E\u0939\u0915
orders.print.eventDetails=\u0915\u093E\u0930\u094D\u092F\u0915\u094D\u0930\u092E \u0924\u092A\u0936\u0940\u0932
orders.print.guests=\u092A\u093E\u0939\u0941\u0923\u0947
orders.print.venue=\u0938\u094D\u0925\u0933
orders.print.menuItems=\u092E\u0947\u0928\u0942 \u0906\u092F\u091F\u092E
orders.print.item=\u0906\u092F\u091F\u092E
orders.print.qty=\u092A\u094D\u0930\u092E\u093E\u0923
orders.print.pricePerItem=\u0915\u093F\u0902\u092E\u0924/\u0906\u092F\u091F\u092E
orders.print.pricePerUnit=\u0915\u093F\u0902\u092E\u0924/\u090F\u0915\u0915
orders.print.subtotal=\u0909\u092A-\u090F\u0915\u0942\u0923
orders.print.menuSubtotal=\u092E\u0947\u0928\u0942 \u0909\u092A-\u090F\u0915\u0942\u0923
orders.print.utilities=\u0909\u092A\u092F\u094B\u0917\u093F\u0924\u093E
orders.print.utilitySubtotal=\u0909\u092A\u092F\u094B\u0917\u093F\u0924\u093E \u0909\u092A-\u090F\u0915\u0942\u0923
orders.print.discount=\u0938\u0935\u0932\u0924
orders.print.tax=\u0915\u0930
orders.print.grandTotal=\u090F\u0915\u0942\u0923 \u0930\u0915\u094D\u0915\u092E
orders.print.paid=\u092D\u0930\u0932\u0947\u0932\u0947
orders.print.balanceDue=\u0936\u093F\u0932\u094D\u0932\u0915 \u0930\u0915\u094D\u0915\u092E
orders.print.notes=\u0928\u094B\u091F\u094D\u0938
orders.print.thankYou=\u0924\u0941\u092E\u091A\u094D\u092F\u093E \u0935\u094D\u092F\u0935\u0938\u093E\u092F\u093E\u092C\u0926\u094D\u0926\u0932 \u0927\u0928\u094D\u092F\u0935\u093E\u0926!
orders.print.generatedOn=\u0924\u092F\u093E\u0930 \u0915\u0947\u0932\u0947\u0932\u0947

# Email Settings (hardcoded strings)
email.settings.configured=\u0908\u092E\u0947\u0932 \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930 \u0906\u0939\u0947
email.settings.notConfigured=\u0908\u092E\u0947\u0932 \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930 \u0928\u093E\u0939\u0940
email.settings.configuredHint=\u0924\u0941\u092E\u091A\u094D\u092F\u093E \u0908\u092E\u0947\u0932 \u0938\u0942\u091A\u0928\u093E \u092A\u093E\u0920\u0935\u093E\u092F\u0932\u093E \u0924\u092F\u093E\u0930 \u0906\u0939\u0947\u0924.
email.settings.notConfiguredHint=\u0908\u092E\u0947\u0932 \u0938\u0942\u091A\u0928\u093E \u0938\u0915\u094D\u0937\u092E \u0915\u0930\u0923\u094D\u092F\u093E\u0938\u093E\u0920\u0940 SMTP \u0938\u0947\u091F\u093F\u0902\u0917\u094D\u091C \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930 \u0915\u0930\u093E.
email.settings.smtpConfig=SMTP \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930\u0947\u0936\u0928
email.settings.smtp.hostHint=\u0909\u0926\u093E. smtp.gmail.com, smtp.sendgrid.net
email.settings.smtp.portHint=\u0938\u093E\u0927\u093E\u0930\u0923\u0924: 587 (TLS) \u0915\u093F\u0902\u0935\u093E 465 (SSL)
email.settings.smtp.passwordHint=Gmail \u0938\u093E\u0920\u0940, App Password \u0935\u093E\u092A\u0930\u093E
email.settings.sendTest=\u091F\u0947\u0938\u094D\u091F \u0908\u092E\u0947\u0932 \u092A\u093E\u0920\u0935\u093E
email.settings.cancel=\u0930\u0926\u094D\u0926 \u0915\u0930\u093E
email.settings.sending=\u092A\u093E\u0920\u0935\u0924 \u0906\u0939\u0947...
email.settings.enterEmail=\u0915\u0943\u092A\u092F\u093E \u0908\u092E\u0947\u0932 \u092A\u0924\u094D\u0924\u093E \u091F\u093E\u0915\u093E
email.settings.sendError=\u091F\u0947\u0938\u094D\u091F \u0908\u092E\u0947\u0932 \u092A\u093E\u0920\u0935\u0924\u093E\u0928\u093E \u0924\u094D\u0930\u0941\u091F\u0940

# Payment Settings (hardcoded strings)
payment.settings.title=\u092A\u0947\u092E\u0947\u0902\u091F \u0938\u0947\u091F\u093F\u0902\u0917\u094D\u091C
payment.settings.enabled=\u092A\u0947\u092E\u0947\u0902\u091F \u0935\u0948\u0936\u093F\u0937\u094D\u091F\u094D\u092F \u0938\u0915\u094D\u0937\u092E \u0906\u0939\u0947
payment.settings.disabled=\u092A\u0947\u092E\u0947\u0902\u091F \u0935\u0948\u0936\u093F\u0937\u094D\u091F\u094D\u092F \u0905\u0915\u094D\u0937\u092E \u0906\u0939\u0947
payment.settings.enabledHint=\u0924\u0941\u092E\u094D\u0939\u0940 \u092A\u0947\u092E\u0947\u0902\u091F \u0928\u094B\u0902\u0926\u0935\u0942 \u0906\u0923\u093F \u091F\u094D\u0930\u0945\u0915 \u0915\u0930\u0942 \u0936\u0915\u0924\u093E.
payment.settings.disabledHint=\u092A\u0947\u092E\u0947\u0902\u091F \u0928\u094B\u0902\u0926\u0923\u0940 \u0938\u0941\u0930\u0942 \u0915\u0930\u0923\u094D\u092F\u093E\u0938\u093E\u0920\u0940 \u0938\u0915\u094D\u0937\u092E \u0915\u0930\u093E.
payment.settings.enableFeature=\u092A\u0947\u092E\u0947\u0902\u091F \u0935\u0948\u0936\u093F\u0937\u094D\u091F\u094D\u092F \u0938\u0915\u094D\u0937\u092E \u0915\u0930\u093E
payment.settings.disabledNote=\u0905\u0915\u094D\u0937\u092E \u0905\u0938\u0924\u093E\u0928\u093E, \u092A\u0947\u092E\u0947\u0902\u091F \u0928\u094B\u0902\u0926\u0923\u0940 \u0909\u092A\u0932\u092C\u094D\u0927 \u0928\u0938\u0947\u0932.
payment.settings.upiTitle=UPI QR \u0915\u094B\u0921 \u0938\u0947\u091F\u093F\u0902\u0917\u094D\u091C
payment.settings.upiDescription=\u092A\u0947\u092E\u0947\u0902\u091F QR \u0915\u094B\u0921 \u0924\u092F\u093E\u0930 \u0915\u0930\u0923\u094D\u092F\u093E\u0938\u093E\u0920\u0940 \u0921\u0940\u092B\u0949\u0932\u094D\u091F UPI \u0924\u092A\u0936\u0940\u0932 \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930 \u0915\u0930\u093E.
payment.settings.defaultUpiId=\u0921\u0940\u092B\u0949\u0932\u094D\u091F UPI ID
payment.settings.upiIdHint=\u0924\u0941\u092E\u091A\u093E UPI \u0935\u094D\u0939\u0930\u094D\u091A\u0941\u0905\u0932 \u092A\u0947\u092E\u0947\u0902\u091F \u0905\u0945\u0921\u094D\u0930\u0947\u0938 (VPA)
payment.settings.upiPayeeName=UPI \u092A\u0947\u092F\u0940 \u0928\u093E\u0935
payment.settings.upiPayeeHint=UPI \u0905\u0945\u092A\u094D\u0938\u092E\u0927\u094D\u092F\u0947 \u092A\u0947\u092F\u0930\u0932\u093E \u0926\u093F\u0938\u0923\u093E\u0930\u0947 \u0928\u093E\u0935
payment.settings.upiReady=UPI QR \u0915\u094B\u0921 \u0924\u092F\u093E\u0930 \u0915\u0930\u0923\u0947 \u0924\u092F\u093E\u0930 \u0906\u0939\u0947.
payment.settings.upiNotReady=QR \u0915\u094B\u0921 \u0924\u092F\u093E\u0930 \u0915\u0930\u0923\u094D\u092F\u093E\u0938\u093E\u0920\u0940 UPI ID \u0906\u0923\u093F \u092A\u0947\u092F\u0940 \u0928\u093E\u0935 \u0926\u094B\u0928\u094D\u0939\u0940 \u092D\u0930\u093E.

# Customers (hardcoded JS confirm)
customers.confirmDelete=\u0924\u0941\u092E\u094D\u0939\u093E\u0932\u093E \u0939\u093E \u0917\u094D\u0930\u093E\u0939\u0915 \u0939\u091F\u0935\u093E\u092F\u091A\u093E \u0906\u0939\u0947 \u0915\u093E?

# Footer
footer.rights=\u0938\u0930\u094D\u0935 \u0939\u0915\u094D\u0915 \u0930\u093E\u0916\u0940\u0935.
footer.adminPortal=SM-Caterer \u0905\u0945\u0921\u092E\u093F\u0928 \u092A\u094B\u0930\u094D\u091F\u0932
"@

# =====================================================
# HINDI - messages_hi.properties
# =====================================================
$hiKeys = @"

# =====================================================
# Additional Keys for Template i18n Conversion
# =====================================================

# Common
common.yes=\u0939\u093E\u0901
common.no=\u0928\u0939\u0940\u0902
common.send=\u092D\u0947\u091C\u0947\u0902
common.close=\u092C\u0902\u0926 \u0915\u0930\u0947\u0902
common.breadcrumb.admin=\u090F\u0921\u092E\u093F\u0928
common.view=\u0926\u0947\u0916\u0947\u0902

# Admin Navigation (for main layout)
admin.nav.dashboard=\u0921\u0948\u0936\u092C\u094B\u0930\u094D\u0921
admin.nav.tenants=\u091F\u0947\u0928\u0947\u0902\u091F
admin.nav.users=\u0909\u092A\u092F\u094B\u0917\u0915\u0930\u094D\u0924\u093E

# Admin Dashboard (hardcoded strings)
admin.dashboard.noTenants=\u0915\u094B\u0908 \u091F\u0947\u0928\u0947\u0902\u091F \u0928\u0939\u0940\u0902 \u092E\u093F\u0932\u0947
admin.dashboard.code=\u0915\u094B\u0921
admin.dashboard.businessName=\u0935\u094D\u092F\u0935\u0938\u093E\u092F \u0928\u093E\u092E
admin.dashboard.contact=\u0938\u0902\u092A\u0930\u094D\u0915
admin.dashboard.status=\u0938\u094D\u0925\u093F\u0924\u093F
admin.dashboard.subscriptionEnd=\u0938\u0926\u0938\u094D\u092F\u0924\u093E \u0938\u092E\u093E\u092A\u094D\u0924\u093F

# Admin Users Form (hardcoded strings)
admin.users.fullName=\u092A\u0942\u0930\u093E \u0928\u093E\u092E
admin.users.passwordNew=\u092A\u093E\u0938\u0935\u0930\u094D\u0921 *
admin.users.breadcrumb.add=\u091C\u094B\u0921\u093C\u0947\u0902
admin.users.breadcrumb.edit=\u0938\u0902\u092A\u093E\u0926\u093F\u0924 \u0915\u0930\u0947\u0902
admin.users.lang.english=English
admin.users.lang.hindi=\u0939\u093F\u0902\u0926\u0940
admin.users.lang.marathi=\u092E\u0930\u093E\u0920\u0940

# Payments (hardcoded strings)
payments.orderDetails=\u0911\u0930\u094D\u0921\u0930 \u0935\u093F\u0935\u0930\u0923
payments.orderNumber=\u0911\u0930\u094D\u0921\u0930 \u0928\u0902\u092C\u0930
payments.customer=\u0917\u094D\u0930\u093E\u0939\u0915
payments.orderTotal=\u0911\u0930\u094D\u0921\u0930 \u0915\u0941\u0932
payments.balanceDue=\u0936\u0947\u0937 \u0930\u093E\u0936\u093F
payments.maximum=\u0905\u0927\u093F\u0915\u0924\u092E
payments.transRefHint=\u0935\u0948\u0915\u0932\u094D\u092A\u093F\u0915: UTR \u0928\u0902\u092C\u0930, \u091A\u0947\u0915 \u0928\u0902\u092C\u0930, \u0906\u0926\u093F.
payments.upiIdHint=\u0935\u0948\u0915\u0932\u094D\u092A\u093F\u0915: UPI \u092D\u0941\u0917\u0924\u093E\u0928 \u0915\u0947 \u0932\u093F\u090F \u092D\u0941\u0917\u0924\u093E\u0928\u0915\u0930\u094D\u0924\u093E \u0915\u093E UPI ID
payments.resendEmail=\u0908\u092E\u0947\u0932 \u092A\u0941\u0928\u0903 \u092D\u0947\u091C\u0947\u0902
payments.emailSent=\u0908\u092E\u0947\u0932 \u092D\u0947\u091C\u093E \u0917\u092F\u093E
payments.viewOrder=\u0911\u0930\u094D\u0921\u0930 \u0926\u0947\u0916\u0947\u0902
payments.viewAllPayments=\u0911\u0930\u094D\u0921\u0930 \u0915\u0947 \u0938\u092D\u0940 \u092D\u0941\u0917\u0924\u093E\u0928 \u0926\u0947\u0916\u0947\u0902

# Orders Print
orders.print.printOrder=\u0911\u0930\u094D\u0921\u0930 \u092A\u094D\u0930\u093F\u0902\u091F \u0915\u0930\u0947\u0902
orders.print.close=\u092C\u0902\u0926 \u0915\u0930\u0947\u0902
orders.print.order=\u0911\u0930\u094D\u0921\u0930
orders.print.customer=\u0917\u094D\u0930\u093E\u0939\u0915
orders.print.eventDetails=\u0907\u0935\u0947\u0902\u091F \u0935\u093F\u0935\u0930\u0923
orders.print.guests=\u092E\u0947\u0939\u092E\u093E\u0928
orders.print.venue=\u0938\u094D\u0925\u093E\u0928
orders.print.menuItems=\u092E\u0947\u0928\u094D\u092F\u0942 \u0906\u0907\u091F\u092E
orders.print.item=\u0906\u0907\u091F\u092E
orders.print.qty=\u092E\u093E\u0924\u094D\u0930\u093E
orders.print.pricePerItem=\u092E\u0942\u0932\u094D\u092F/\u0906\u0907\u091F\u092E
orders.print.pricePerUnit=\u092E\u0942\u0932\u094D\u092F/\u0907\u0915\u093E\u0908
orders.print.subtotal=\u0909\u092A-\u092F\u094B\u0917
orders.print.menuSubtotal=\u092E\u0947\u0928\u094D\u092F\u0942 \u0909\u092A-\u092F\u094B\u0917
orders.print.utilities=\u0909\u092A\u092F\u094B\u0917\u093F\u0924\u093E\u090F\u0902
orders.print.utilitySubtotal=\u0909\u092A\u092F\u094B\u0917\u093F\u0924\u093E \u0909\u092A-\u092F\u094B\u0917
orders.print.discount=\u091B\u0942\u091F
orders.print.tax=\u0915\u0930
orders.print.grandTotal=\u0915\u0941\u0932 \u0930\u093E\u0936\u093F
orders.print.paid=\u092D\u0941\u0917\u0924\u093E\u0928
orders.print.balanceDue=\u0936\u0947\u0937 \u0930\u093E\u0936\u093F
orders.print.notes=\u0928\u094B\u091F\u094D\u0938
orders.print.thankYou=\u0906\u092A\u0915\u0947 \u0935\u094D\u092F\u0935\u0938\u093E\u092F \u0915\u0947 \u0932\u093F\u090F \u0927\u0928\u094D\u092F\u0935\u093E\u0926!
orders.print.generatedOn=\u0924\u0948\u092F\u093E\u0930 \u0915\u093F\u092F\u093E \u0917\u092F\u093E

# Email Settings (hardcoded strings)
email.settings.configured=\u0908\u092E\u0947\u0932 \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930 \u0939\u0948
email.settings.notConfigured=\u0908\u092E\u0947\u0932 \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930 \u0928\u0939\u0940\u0902 \u0939\u0948
email.settings.configuredHint=\u0906\u092A\u0915\u0940 \u0908\u092E\u0947\u0932 \u0938\u0942\u091A\u0928\u093E\u090F\u0902 \u092D\u0947\u091C\u0928\u0947 \u0915\u0947 \u0932\u093F\u090F \u0924\u0948\u092F\u093E\u0930 \u0939\u0948\u0902\u0964
email.settings.notConfiguredHint=\u0908\u092E\u0947\u0932 \u0938\u0942\u091A\u0928\u093E\u090F\u0902 \u0938\u0915\u094D\u0937\u092E \u0915\u0930\u0928\u0947 \u0915\u0947 \u0932\u093F\u090F SMTP \u0938\u0947\u091F\u093F\u0902\u0917\u094D\u0938 \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930 \u0915\u0930\u0947\u0902\u0964
email.settings.smtpConfig=SMTP \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930\u0947\u0936\u0928
email.settings.smtp.hostHint=\u0909\u0926\u093E. smtp.gmail.com, smtp.sendgrid.net
email.settings.smtp.portHint=\u0938\u093E\u092E\u093E\u0928\u094D\u092F\u0924: 587 (TLS) \u092F\u093E 465 (SSL)
email.settings.smtp.passwordHint=Gmail \u0915\u0947 \u0932\u093F\u090F, App Password \u0909\u092A\u092F\u094B\u0917 \u0915\u0930\u0947\u0902
email.settings.sendTest=\u091F\u0947\u0938\u094D\u091F \u0908\u092E\u0947\u0932 \u092D\u0947\u091C\u0947\u0902
email.settings.cancel=\u0930\u0926\u094D\u0926 \u0915\u0930\u0947\u0902
email.settings.sending=\u092D\u0947\u091C \u0930\u0939\u0947 \u0939\u0948\u0902...
email.settings.enterEmail=\u0915\u0943\u092A\u092F\u093E \u0908\u092E\u0947\u0932 \u092A\u0924\u093E \u0926\u0930\u094D\u091C \u0915\u0930\u0947\u0902
email.settings.sendError=\u091F\u0947\u0938\u094D\u091F \u0908\u092E\u0947\u0932 \u092D\u0947\u091C\u0928\u0947 \u092E\u0947\u0902 \u0924\u094D\u0930\u0941\u091F\u093F

# Payment Settings (hardcoded strings)
payment.settings.title=\u092D\u0941\u0917\u0924\u093E\u0928 \u0938\u0947\u091F\u093F\u0902\u0917\u094D\u0938
payment.settings.enabled=\u092D\u0941\u0917\u0924\u093E\u0928 \u0938\u0941\u0935\u093F\u0927\u093E \u0938\u0915\u094D\u0937\u092E \u0939\u0948
payment.settings.disabled=\u092D\u0941\u0917\u0924\u093E\u0928 \u0938\u0941\u0935\u093F\u0927\u093E \u0905\u0915\u094D\u0937\u092E \u0939\u0948
payment.settings.enabledHint=\u0906\u092A \u092D\u0941\u0917\u0924\u093E\u0928 \u0930\u093F\u0915\u0949\u0930\u094D\u0921 \u0914\u0930 \u091F\u094D\u0930\u0948\u0915 \u0915\u0930 \u0938\u0915\u0924\u0947 \u0939\u0948\u0902\u0964
payment.settings.disabledHint=\u092D\u0941\u0917\u0924\u093E\u0928 \u0930\u093F\u0915\u0949\u0930\u094D\u0921\u093F\u0902\u0917 \u0936\u0941\u0930\u0942 \u0915\u0930\u0928\u0947 \u0915\u0947 \u0932\u093F\u090F \u0938\u0915\u094D\u0937\u092E \u0915\u0930\u0947\u0902\u0964
payment.settings.enableFeature=\u092D\u0941\u0917\u0924\u093E\u0928 \u0938\u0941\u0935\u093F\u0927\u093E \u0938\u0915\u094D\u0937\u092E \u0915\u0930\u0947\u0902
payment.settings.disabledNote=\u0905\u0915\u094D\u0937\u092E \u0939\u094B\u0928\u0947 \u092A\u0930, \u092D\u0941\u0917\u0924\u093E\u0928 \u0930\u093F\u0915\u0949\u0930\u094D\u0921\u093F\u0902\u0917 \u0909\u092A\u0932\u092C\u094D\u0927 \u0928\u0939\u0940\u0902 \u0939\u094B\u0917\u0940\u0964
payment.settings.upiTitle=UPI QR \u0915\u094B\u0921 \u0938\u0947\u091F\u093F\u0902\u0917\u094D\u0938
payment.settings.upiDescription=\u092D\u0941\u0917\u0924\u093E\u0928 QR \u0915\u094B\u0921 \u092C\u0928\u093E\u0928\u0947 \u0915\u0947 \u0932\u093F\u090F \u0921\u093F\u092B\u0949\u0932\u094D\u091F UPI \u0935\u093F\u0935\u0930\u0923 \u0915\u0949\u0928\u094D\u092B\u093F\u0917\u0930 \u0915\u0930\u0947\u0902\u0964
payment.settings.defaultUpiId=\u0921\u093F\u092B\u0949\u0932\u094D\u091F UPI ID
payment.settings.upiIdHint=\u0906\u092A\u0915\u093E UPI \u0935\u0930\u094D\u091A\u0941\u0905\u0932 \u092A\u0947\u092E\u0947\u0902\u091F \u090F\u0921\u094D\u0930\u0947\u0938 (VPA)
payment.settings.upiPayeeName=UPI \u092A\u0947\u092F\u0940 \u0928\u093E\u092E
payment.settings.upiPayeeHint=UPI \u0910\u092A\u094D\u0938 \u092E\u0947\u0902 \u092D\u0941\u0917\u0924\u093E\u0928\u0915\u0930\u094D\u0924\u093E \u0915\u094B \u0926\u093F\u0916\u093E\u092F\u093E \u091C\u093E\u0928\u0947 \u0935\u093E\u0932\u093E \u0928\u093E\u092E
payment.settings.upiReady=UPI QR \u0915\u094B\u0921 \u092C\u0928\u093E\u0928\u0947 \u0915\u0947 \u0932\u093F\u090F \u0924\u0948\u092F\u093E\u0930 \u0939\u0948\u0964
payment.settings.upiNotReady=QR \u0915\u094B\u0921 \u092C\u0928\u093E\u0928\u0947 \u0915\u0947 \u0932\u093F\u090F UPI ID \u0914\u0930 \u092A\u0947\u092F\u0940 \u0928\u093E\u092E \u0926\u094B\u0928\u094B\u0902 \u092D\u0930\u0947\u0902\u0964

# Customers (hardcoded JS confirm)
customers.confirmDelete=\u0915\u094D\u092F\u093E \u0906\u092A \u0935\u093E\u0915\u0908 \u0907\u0938 \u0917\u094D\u0930\u093E\u0939\u0915 \u0915\u094B \u0939\u091F\u093E\u0928\u093E \u091A\u093E\u0939\u0924\u0947 \u0939\u0948\u0902?

# Footer
footer.rights=\u0938\u0930\u094D\u0935\u093E\u0927\u093F\u0915\u093E\u0930 \u0938\u0941\u0930\u0915\u094D\u0937\u093F\u0924\u0964
footer.adminPortal=SM-Caterer \u090F\u0921\u092E\u093F\u0928 \u092A\u094B\u0930\u094D\u091F\u0932
"@

# Append to each file
Add-Content -Path "$basePath\messages.properties" -Value $enKeys -Encoding UTF8
Write-Host "Added new keys to messages.properties" -ForegroundColor Green

Add-Content -Path "$basePath\messages_mr.properties" -Value $mrKeys -Encoding UTF8
Write-Host "Added new keys to messages_mr.properties" -ForegroundColor Green

Add-Content -Path "$basePath\messages_hi.properties" -Value $hiKeys -Encoding UTF8
Write-Host "Added new keys to messages_hi.properties" -ForegroundColor Green

# Verify counts
$en = (Get-Content "$basePath\messages.properties" | Where-Object { $_ -match '^[a-zA-Z]' }).Count
$mr = (Get-Content "$basePath\messages_mr.properties" | Where-Object { $_ -match '^[a-zA-Z]' }).Count
$hi = (Get-Content "$basePath\messages_hi.properties" | Where-Object { $_ -match '^[a-zA-Z]' }).Count
Write-Host "`nKey counts - English: $en, Marathi: $mr, Hindi: $hi" -ForegroundColor Cyan
