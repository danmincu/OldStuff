using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ReceiptGen
{
    public class Receipt
    {
        const string LANDLORD = "Dan Mincu";
        const string TENANTS = "CHUNG MEI CHUN JOSEPHINE,YEUNG CHAU HEI CHARLES and YEUNG SUM WAH";
        //const string TENANTS = "HAIDER FARIS MOHAMMED";
        const string ADDRESS = "#1301 - 234 Rideau Street, Ottawa, K1N 0A9, ON";
        //const string ADDRESS = "Parking C41 - 234 Rideau Street, Ottawa, K1N 0A9, ON";
        static DateTime StartRental = new DateTime(2017, 4, 1);
        static DateTime EndRental = new DateTime(2017, 8, 1);

        static string htmlTemplate = @"<div style=""border-style:solid; border-width: 1px; padding: 10px;  margin: 25px 25px 10px 65px; page-break-inside: avoid; background: lightgrey"" >
<h1  style=""text-align: center;border-style: solid;border-width: 2px .2px 2px .2px; padding: 30px; background: darkgrey"">
RENT RECEIPT
</h1>
<div style=""text-align: right;margin-top: -70px;margin-bottom: 20px; margin-right:10px"" >
Receipt:#{0}
</div>

<div style=""text-align: right;margin-top: -15px;margin-bottom: 20px; margin-right:10px"">
Date:{1}
</div>
<div style=""margin-left: 10px; margin-top:10px"" >
Payer:<b>{4}</b>
</div>


<div style=""text-align: right;margin-top: -16px;margin-bottom: 20px; margin-left: 70%; border-width: 2px; border-style:solid; padding: 3px"">
Amount: <b>${5}</b>
</div>
<div style = ""margin-left: 10px; margin-top:20px"" >
Address: <b>{2}</b>
</div>
<div style = ""margin-left: 10px; margin-top:20px"" >
Landlord: <b>{3}</b>
</div>
<div style = ""margin-left: 10px; margin-top:20px"" >
For the period month/year: <b>{6}</b>
</div>
<div style=""text-align: left;margin-top: -16px;margin-bottom: 20px; margin-left: 70%; border-width: 1px; border-style:dotted; padding:3px 3px 0px 3px"">
Payee receipt signature:
<img id=""
45
+
red_flower"" width=""180"" src=""signature.png"">
</div>
</div>
";

        static string cutScisors = @"<img width=""32"" height=""32"" src=""scissors.png"">
            <div style=""border-width: 4px 0px 0px 0px;border-style:dotted;margin-left: 42px;margin-top: -18px;""></div>";

        public static void GenerateHtml()
        {
            File.WriteAllText("AllReceipts.html", "<html><head></head><body>" + Receipt.GeneratePeriod() + "</body></html>");
        }
        public static string GeneratePeriod()
        {
            string html = "";
            Receipt r = new Receipt();

            var currentDate = StartRental;
            int i = 0;
            while (currentDate < EndRental)
            {
                var month = currentDate.Month;
                var year = currentDate.Year;
                html += r.GenerateMonth(year, month, ++i);
                if (month % 2 == 0)
                    html += cutScisors;
                month++;
                if (month > 12)
                {
                    month = 1;
                    year++;
                }
                currentDate = new DateTime(year, month, 1);
            }

            return html;
        }

        Dictionary<int, int> monthlyRentalPerYear = new Dictionary<int, int>() { { 2011, 2100},
        { 2012, 2100},
        { 2013, 2100},
        { 2014, 2100},
        { 2015, 1925},
        { 2016, 1950},
        { 2017, 1950}};
        //{ 2017, 140}};

        public string GenerateMonth(int year, int month, int receiptNumber)
        {
            return String.Format(htmlTemplate, receiptNumber,
                new DateTime(year, month, 1).ToString("MMMM", CultureInfo.InvariantCulture) + " 1st, " + year.ToString(), ADDRESS, LANDLORD, TENANTS, monthlyRentalPerYear[year], month.ToString() + "/" + year.ToString());
        }
    }
}

