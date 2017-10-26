using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace MvcUploadFile.Controllers
{
    public class PhotoUploadController : Controller
    {
        //
        // GET: /PhotoUpload/

        public ActionResult Index()
        {
            return View();
        }

        [HttpPost]
        [AllowAnonymous]
        public ActionResult Upload(UploadFileModel fileModel)//(HttpPostedFileWrapper photo)//
        {
            
            if (ModelState.IsValid)
            {
                if (fileModel != null && fileModel.Photo != null)
                { }

                return RedirectToAction("Index");
            }

            
            return new ContentResult() { Content = ModelState.Values.FirstOrDefault().Errors.FirstOrDefault().ErrorMessage };



            
        }

        //public ActionResult Upload()
        //{
        //    return RedirectToAction("Index");
        //}

        [HttpPost]
        [AllowAnonymous]
        public ActionResult MultipleUpload(IEnumerable<HttpPostedFileBase> files)
        {
            return RedirectToAction("Index");
        }
    }


    public class UploadFileModel
    {
        [FileSize(1024000)]
        [FileTypes("jpg,jpeg,png")]
        public HttpPostedFileWrapper Photo { get; set; }
    }

    public class FileSizeAttribute : ValidationAttribute, IClientValidatable 
    {
        private readonly int _maxSize;

        public FileSizeAttribute(int maxSize)
        {
            _maxSize = maxSize;
        }

        public override bool IsValid(object value)
        {
            if (value == null) return true;

            return _maxSize > (value as HttpPostedFileWrapper).ContentLength;
        }

        public override string FormatErrorMessage(string name)
        {
            return string.Format("The file size should not exceed {0}", _maxSize);
        }

        public IEnumerable<ModelClientValidationRule> GetClientValidationRules(ModelMetadata metadata, ControllerContext context)
        {
            var returnVar = new ModelClientValidationRule
            {
                ErrorMessage = "File is too large",
                ValidationType = "maxfilesize",
            };
            returnVar.ValidationParameters.Add("size", _maxSize);
            yield return returnVar;
        }
    }

    public class FileTypesAttribute : ValidationAttribute, IClientValidatable 
    {
        private readonly List<string> _types;

        public FileTypesAttribute(string types)
        {
            _types = types.Split(',').ToList();
        }

        public override bool IsValid(object value)
        {
            if (value == null) return true;

            var fileExt = System.IO.Path.GetExtension((value as HttpPostedFileWrapper).FileName).Substring(1);
            return _types.Contains(fileExt, StringComparer.OrdinalIgnoreCase);
        }

        public override string FormatErrorMessage(string name)
        {
            return string.Format("Invalid file type. Only the following types {0} are supported.", String.Join(", ", _types));
        }

        public IEnumerable<ModelClientValidationRule> GetClientValidationRules(ModelMetadata metadata, ControllerContext context)
        {
            var returnVar = new ModelClientValidationRule
            {
                ErrorMessage = "Invalid file type. Only {0} are supported.",
                ValidationType = "filetypes",
            };
            returnVar.ValidationParameters.Add("types", string.Join(",", _types));
            yield return returnVar;
        }
    }
}
