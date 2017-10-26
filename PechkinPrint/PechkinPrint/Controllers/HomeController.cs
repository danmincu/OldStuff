using System;
using System.Collections.Generic;
using System.Drawing.Printing;
using System.IO;
using System.Linq;
using System.Net;
using System.Web;
using System.Web.Mvc;
using TuesPechkin;

namespace PechkinPrint.Controllers
{
    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            return View();
        }

        public ActionResult PrintFile()
        {
            // create a new document with your desired configuration
            var document = new HtmlToPdfDocument
            {
                GlobalSettings =
                {
                    ProduceOutline = true,
                    DocumentTitle = "Pretty Websites",
                    PaperSize = PaperKind.A2,                   
                    Orientation = GlobalSettings.PaperOrientation.Landscape,
                    
                    
                    Margins =
                    {
                        All = 1.375,
                        Unit = Unit.Centimeters
                    }
                },
                Objects = {
                    new ObjectSettings { HtmlText = "<h1>Pretty Websites</h1><p>This might take a bit to convert!</p>" },
                    new ObjectSettings {
                        PageUrl = "danix.cloudapp.net/Bracket/Bracket/Print?divisionID=b83d89ff-8eff-448c-82d9-eb31a49accad",
                        //PageUrl = "danix.cloudapp.net/Bracket/Tournaments/Print/3cd50486-7d48-43d5-bcab-32a829817d76"
                    WebSettings = new WebSettings() {EnableJavascript=true, LoadImages=true,PrintMediaType=false}, LoadSettings = new LoadSettings() { ZoomFactor = 0.65} }
                    //new ObjectSettings { PageUrl = "www.google.com" },
                    //new ObjectSettings { PageUrl = "www.microsoft.com" },
                    //new ObjectSettings { PageUrl = "www.github.com" }
                }
            };
            const string TEST_WK_VER = "0.12.2";
            //var toolset = new RemotingToolset<PdfToolset>(new StaticDeployment(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"wk-ver",TEST_WK_VER)));
       
            //* working
            //var toolset = new RemotingToolset<PdfToolset>(new Win32EmbeddedDeployment(new TempFolderDeployment()));

            //IConverter converter = new ThreadSafeConverter(toolset);
            //*end wokring

            var converter = MvcApplication.Converter;

            //new TuesPechkin.StandardConverter(null);//  Factory.Create();

            // subscribe to events
            /* converter.Begin += OnBegin;
            converter.Error += OnError;
            converter.Warning += OnWarning;
            converter.PhaseChanged += OnPhase;
            converter.ProgressChanged += OnProgress;
            converter.Finished += OnFinished;*/

            // convert document
            byte[] result = converter.Convert(document);

            ByteArrayToFile(result, "C:/temp/exmple.pdf");

            return new HttpStatusCodeResult(HttpStatusCode.OK);
        }

        public static bool ByteArrayToFile(byte[] _ByteArray, string _FileName)
        {
            try
            {
                System.IO.FileStream _FileStream = new System.IO.FileStream(_FileName, System.IO.FileMode.Create, System.IO.FileAccess.Write);

                _FileStream.Write(_ByteArray, 0, _ByteArray.Length);

                _FileStream.Close();

                return true;
            }
            catch (Exception _Exception)
            {
                Console.WriteLine("Exception caught in process: {0}", _Exception.ToString());
            }

            return false;
        }

        public ActionResult About()
        {
            ViewBag.Message = "Your application description page.";

            return View();
        }


        public ActionResult Print(Uri uri)
        {
            if (uri == null)
            {
                return new HttpStatusCodeResult(HttpStatusCode.BadRequest);
            }

            //var pechkin = Factory.Create(new GlobalConfig().SetPaperSize(System.Drawing.Printing.PaperKind.Letter));
            //ObjectConfig oc = new ObjectConfig();
            //// and set it up using fluent notation too
            //oc.SetCreateExternalLinks(false)
            //  .SetLoadImages(true)
            //  //  .SetUserStylesheetUri("http://danix.cloudapp.net/Bracket/Content/jquery.bracket.john.css")
            //  //  .SetRenderDelay(3000)
            //  .SetRunJavascript(true)
            //  //   .SetJavascriptDebugMode(true)
            //  .SetPrintBackground(true)
            //  .SetZoomFactor(0.77)
            //  //.SetPageUri("http://danix.cloudapp.net/Bracket/Tournaments/Print/2cc9a80e-53cb-49b7-a46b-a6c524c822e8");
            //  .SetPageUri(uri.ToString());
            //var pdf = pechkin.Convert(oc);
            //return new FileContentResult(pdf, "application/pdf");

            var document = new HtmlToPdfDocument
            {
                GlobalSettings =
                {
                    ProduceOutline = true,
                    DocumentTitle = "Pretty Websites",
                    PaperSize = PaperKind.Letter,                   
                    
                    //PaperSize = PaperKind.A4,
                    Margins =
                    {
                        All = 1.375,
                        Unit = Unit.Centimeters
                    }
                },
                Objects = {
                    //new ObjectSettings { HtmlText = "<h1>Pretty Websites</h1><p>This might take a bit to convert!</p>" },
                    new ObjectSettings {
                        PageUrl = uri.ToString(),
                        WebSettings = new WebSettings() {EnableJavascript=true, LoadImages=true,PrintMediaType=false},
                        LoadSettings = new LoadSettings() { ZoomFactor = 0.77} }
                    //new ObjectSettings { PageUrl = "www.google.com" },
                    //new ObjectSettings { PageUrl = "www.microsoft.com" },
                    //new ObjectSettings { PageUrl = "www.github.com" }
                }
            };
            const string TEST_WK_VER = "0.12.2";
            //var toolset = new RemotingToolset<PdfToolset>(new StaticDeployment(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"wk-ver",TEST_WK_VER)));

            //* working
            //var toolset = new RemotingToolset<PdfToolset>(new Win32EmbeddedDeployment(new TempFolderDeployment()));

            //IConverter converter = new ThreadSafeConverter(toolset);
            //*end wokring

            var converter = MvcApplication.Converter;

            //new TuesPechkin.StandardConverter(null);//  Factory.Create();

            // subscribe to events
            /* converter.Begin += OnBegin;
            converter.Error += OnError;
            converter.Warning += OnWarning;
            converter.PhaseChanged += OnPhase;
            converter.ProgressChanged += OnProgress;
            converter.Finished += OnFinished;*/

            // convert document
            byte[] result = converter.Convert(document);

            //ByteArrayToFile(result, "C:/temp/exmple.pdf");

            return new FileContentResult(result, "application/pdf");

        }

        public ActionResult Contact()
        {
            ViewBag.Message = "Your contact page.";

            return View();
        }
    }
}