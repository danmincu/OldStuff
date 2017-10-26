using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Codaxy.WkHtmlToPdf;

namespace Wkhtml2pdfWrapper
{
    class Program
    {
        static void Main(string[] args)
        {
            PdfConvert.ConvertHtmlToPdf(new PdfDocument() { Url = "http://danix.cloudapp.net/Bracket/Bracket/Index?divisionID=75e0640e-c6de-43eb-afba-4cd9f7965253" }, new PdfOutput() { OutputFilePath = "out.pdf" });
            }
    }
}
