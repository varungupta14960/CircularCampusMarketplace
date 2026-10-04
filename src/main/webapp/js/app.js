// CampusCycle - client-side UI convenience only.
// Nothing here performs validation or security checks; all real
// validation and authorization happens server-side in the Servlets/DAOs.

document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss flash alerts after a few seconds.
    document.querySelectorAll('.alert').forEach(function (alertEl) {
        setTimeout(function () {
            if (window.bootstrap && bootstrap.Alert) {
                var instance = bootstrap.Alert.getOrCreateInstance(alertEl);
                instance.close();
            }
        }, 6000);
    });
});
