using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace RentalReceipts
{
    class Program
    {
        static void Main(string[] args)
        {
            //using (var f = new FileStream("AllReceipts.txt", FileMode.CreateNew))
            {
                File.WriteAllText("AllReceipts.txt",Receipt.GeneratePeriod());
            }
        }
    }
}
