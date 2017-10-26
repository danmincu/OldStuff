using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RentalReceipts
{
    public class Receipt
    {
        const string LANDLORD = "Dan Mincu";
        const string TENANTS = "Amr Mahmoud & Mohamed Mahmoud";
        const string ADDRESS = "#1301 - 234 Rideau Street, Ottawa, K1N 0A9, ON";
        static DateTime StartRental = new DateTime(2016, 9, 1);
        static DateTime EndRental = new DateTime(2016, 11, 30);
        static string receiptTemplate = @"

----------------------------------------------------------------------------------
RENT RECEIPT NO. {0}
Date = {1} 
Property address = {2}
Landlord name: {3}
Payer name: {4}
Amount: ${5}
For the period: {6}
----------------------------------------------------------------------------------


";
        public static void GenerateHtml()
        {
            File.WriteAllText("AllReceipts.txt", Receipt.GeneratePeriod());
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
        { 2016, 1950}};

        public string GenerateMonth(int year, int month, int receiptNumber)
        {
            return String.Format(receiptTemplate, receiptNumber,
                new DateTime(year, month, 1).ToString("MMMM", CultureInfo.InvariantCulture) + " 1st," + year.ToString(), ADDRESS, LANDLORD, TENANTS, monthlyRentalPerYear[year], month.ToString() + "/" + year.ToString());

        }


    }
}
