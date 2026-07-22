// =====================================================
// UpScan upload
// =====================================================
$("#uploadForm").submit(function (e) {
    e.preventDefault();
    const fileLength = $("#file-upload")[0].files.length;
    var errorRequestId = $("[name='x-amz-meta-request-id']").val();

    if (fileLength === 0) {
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=invalidargument&errorMessage=filenotselected&errorRequestId=" + errorRequestId;
        window.location = errorUrl;
    } else if (isFileEmpty()){
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=invalidargument&errorMessage=fileisempty&errorRequestId=" + errorRequestId;
        window.location = errorUrl;
    } else if (isFileNameInvalidLength()) {
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=invalidargument&errorMessage=invalidfilenamelength&errorRequestId=" + errorRequestId;
        window.location = errorUrl;
    } else if (isFileNameContainsDisallowedCharacters()) {
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=invalidargument&errorMessage=disallowedcharacters&errorRequestId=" + errorRequestId;
        window.location = errorUrl;
    } else {
        function disableFileUpload() {
            $("#file-upload").attr('disabled', 'disabled')
        }

        function addUploadSpinner() {
            $("#processing").empty();
            $("#processing").append('<div id="spinning-wheel"><p class="govuk-visually-hidden">' + $("#processingMessage").val() + '</p><div><svg class="ccms-loader" height="100" width="100"><circle cx="50" cy="50" r="40"  fill="none"/></svg></div></div>');
            $(".govuk-form-group--error").removeClass("govuk-form-group--error");
            $("#file-upload-error").remove();
            $("#error-summary").remove();
            $("#submit").remove();
        }

        addUploadSpinner();
        setTimeout(function () {
            this.submit();
            disableFileUpload();
        }.bind(this), 0);
    }

});

function isFileNameInvalidLength() {
    var fileName = $("#file-upload")[0].files[0].name;
    var trimmedFileName = fileName.replace(/\.xml$/i, "");
    if (trimmedFileName.length > 100) {
        return true;
    }
    return false;
}

function isFileNameContainsDisallowedCharacters() {
    var fileName = $("#file-upload")[0].files[0].name;
    var trimmedFileName = fileName.replace(/\.xml$/i, "");
    return /[<>:"'&\/\\|?*]/.test(trimmedFileName);
}

function isFileEmpty() {
    var fileSize = $("#file-upload")[0].files[0].size;
    return fileSize === 0;
}

$(document).ready(function () {
    var hasError = (window.location.href.indexOf("errorCode") > -1);
    var currentTitle = $("title").html();

    if (hasError && !currentTitle.startsWith("Error:")) {
        $("title").html("Error: " + currentTitle);
    }
});