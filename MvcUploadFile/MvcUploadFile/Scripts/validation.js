
(function () {
        //function from unobtrusive:
        function setValidationValues(options, ruleName, value) {
            options.rules[ruleName] = value;
            if (options.message) {
                options.messages[ruleName] = options.message;
            }
        }
        function fileinput(element, validationFunc) {
            var isValid = 'pending',
                i = 0,
                totalLoops = (element.files || []).length;
            while (i < totalLoops && (isValid !== false)) {
                isValid = validationFunc.call(element, element.files[i]);
                i += 1;
            }
            return isValid;
        }
        //------------------------
        $.validator.addMethod("maxfilesize", function (value, element, maxSize) {
            return fileinput(element, function (f) {
                return f.size < maxSize;
            })
        });
        $.validator.unobtrusive.adapters.add("maxfilesize", ["size"], function (options) {
            setValidationValues(options, "maxfilesize", parseInt(options.params.size, 10));
        });
        //------------------------
        $.validator.addMethod("filetypes", function (value, element, acceptedTypes) {
            var getFileType = /\.[^.]+$/;
            return fileinput(element, function (f) {
                var filetype = getFileType.exec(f.name);
                return filetype && $.inArray(filetype[0].substr(1), acceptedTypes) > -1;
            })
        });
        $.validator.unobtrusive.adapters.add("filetypes", ["types"], function (options) {
            var acceptedTypes = options.params.types.split(",");
            options.message = options.message.replace("{0}", acceptedTypes.join(", "));
            setValidationValues(options, "filetypes", acceptedTypes);
        });
    }())   
    
//$(function () {
//    //function from unobtrusive:
//    function setValidationValues(options, ruleName, value) {
//        options.rules[ruleName] = value;
//        if (options.message) {
//            options.messages[ruleName] = options.message;
//        }
//    }
//    function fileinput(element, validationFunc) {
//        var isValid = 'pending',
//            i = 0,
//            totalLoops = (element.files || []).length;
//        while (i < totalLoops && (isValid !== false)) {
//            isValid = validationFunc.call(element, element.files[i]);
//            i += 1;
//        }
//        return isValid;
//    }
//    //------------------------
//    $.validator.addMethod("maxfilesize", function (value, element, maxSize) {
//        return fileinput(element, function (f) {
//            return f.size < maxSize;
//        })
//    });
//    $.validator.unobtrusive.adapters.add("maxfilesize", ["size"], function (options) {
//        setValidationValues(options, "maxfilesize", parseInt(options.params.size, 10));
//    });
//    //------------------------
//    $.validator.addMethod("filetypes", function (value, element, acceptedTypes) {
//        var getFileType = /\.[^.]+$/;
//        return fileinput(element, function (f) {
//            var filetype = getFileType.exec(f.name);
//            return filetype && $.inArray(filetype[0].substr(1), acceptedTypes) > -1;
//        })
//    });
//    $.validator.unobtrusive.adapters.add("filetypes", ["types"], function (options) {
//        var acceptedTypes = options.params.types.split(",");
//        options.message = options.message.replace("{0}", acceptedTypes.join(", "));
//        setValidationValues(options, "filetypes", acceptedTypes);
//    });
//}())
