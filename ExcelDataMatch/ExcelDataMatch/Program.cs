using System;
using System.Collections.Generic;
using System.Linq;
using Excel = Microsoft.Office.Interop.Excel;
using System.IO;

namespace ExcelDataMatch
{
    class Program
    {
        static void Main(string[] args)
        {
            //SaveToWorksheet();
            //Console.ReadLine();
            //return;

            if (args.Count() < 2)
            {
                Console.WriteLine("Specify the dictionary file and the file to parse");
                Console.WriteLine(@"Example: ExcelDataMatch ""ITE calc.xlsx"" ""16 spe.xlsx"" ");
            }
            else
            {

                int maxNumber = 128;
                if (args.Count() == 3)
                    int.TryParse(args[2], out maxNumber);

                var dictionary = new Dictionary<string, int>();
                var dictionaryFileName = System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().CodeBase) + System.IO.Path.DirectorySeparatorChar + args[0]; //@"C:\Users\Dan\Desktop\sarahSam\ITE calc.xlsx";

                Excel.Application MyApp = null;
                Excel.Workbook MyBook = null;
                try
                {
                    //build up the dictionary
                    MyApp = new Excel.Application();

                    if (MyApp == null)
                    {
                        Console.WriteLine("EXCEL could not be started. Check that your office installation and project references are correct.");
                        return;
                    }

                    MyApp.Visible = false;
                    MyBook = MyApp.Workbooks.Open(dictionaryFileName);
                    Console.ForegroundColor = ConsoleColor.Cyan;
                    Console.WriteLine("");
                    Console.WriteLine("*******************************************************************************");
                    Console.WriteLine("************************ EXCEL DATA MATCH *************************************");
                    Console.WriteLine("*******************************************************************************");
                    Console.WriteLine("*******************************************************************************");
                    Console.WriteLine("");
                    Console.ForegroundColor = ConsoleColor.White;


                    Console.BackgroundColor = ConsoleColor.DarkGreen;
                    Console.WriteLine("");
                    Console.WriteLine("Building the NumMatch dictionary...");
                    Console.BackgroundColor = ConsoleColor.Black;
                    Console.WriteLine("");
                    

                    Console.WriteLine("Dictionary will be build out of " + MyBook.Sheets.Count + " sheets \n");
                    Console.ForegroundColor = ConsoleColor.White;

                    for (int sheetNumber = 1; sheetNumber <= Math.Min(maxNumber, MyBook.Sheets.Count); sheetNumber++)
                    {
                        var sheet = (Excel.Worksheet)MyBook.Sheets[sheetNumber]; // Explict cast is not required here
                        Console.WriteLine("Processing <" + sheet.Name + "> sheet " + sheetNumber + "/" + MyBook.Sheets.Count);
                        ReadMatchCountDictionary(dictionary, sheet);
                    }
                }
                finally
                {
                    if (MyBook != null)
                        MyBook.Saved = true;
                    if (MyApp != null)
                        MyApp.Quit();
                    //end build
                }

                //construct sheets
                MyApp = new Excel.Application();
                MyApp.Visible = false;
                try
                {
                    MyBook = MyApp.Workbooks.Open(System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().CodeBase) + System.IO.Path.DirectorySeparatorChar + args[1]); //@"C:\Users\Dan\Desktop\sarahSam\16 spe.xlsx");


                    //SAVING EXCEL
                    Excel.Application xlApp = new Excel.Application();
                    xlApp.Visible = true;
                    Excel.Workbook wb = xlApp.Workbooks.Add(Excel.XlWBATemplate.xlWBATWorksheet);

                    Console.BackgroundColor = ConsoleColor.DarkGreen;
                    Console.WriteLine("");
                    Console.WriteLine("Read data from file...");
                    Console.BackgroundColor = ConsoleColor.Black;
                    Console.WriteLine("");                    

                    Console.WriteLine(MyBook.Sheets.Count + " sheets will be processed.");
                    for (int sheetNumber = 1; sheetNumber <= Math.Min(maxNumber, MyBook.Sheets.Count); sheetNumber++)
                    {
                        Console.WriteLine("Processing Sheet " + sheetNumber + "/" + MyBook.Sheets.Count);
                        var worksheet = (Excel.Worksheet)MyBook.Sheets[sheetNumber]; // Explict cast is not required here

                        Console.WriteLine("Processing <" + worksheet.Name + "> sheet " + sheetNumber + "/" + MyBook.Sheets.Count);

                        var genes = ReadFileAndSheet(worksheet);
                        
                        SaveToWorksheet(genes, dictionary, worksheet.Name, wb);

                        //var workSheetfileName = worksheet.Name + ".csv";
                        //if (IsFileNameValid(workSheetfileName))
                        //    SaveToFile(genes, dictionary, workSheetfileName);
                        //else
                        //    SaveToFile(genes, dictionary, Guid.NewGuid().ToString() + ".csv");
                    }
                }
                finally
                {
                    if (MyBook != null)
                        MyBook.Saved = true;
                    if (MyApp != null)
                        MyApp.Quit();
                    //end build
                }
                //end 
            }
            Console.WriteLine("");
            Console.ForegroundColor = ConsoleColor.Yellow;
            Console.WriteLine("");
            Console.WriteLine("DONE! Don't forget to save your Excel file!");
            Console.WriteLine(" ... press any key to exit!");
            Console.ReadLine();
            Console.ForegroundColor = ConsoleColor.White;
        }

        static bool IsFileNameValid(string fileName)
        {
            System.IO.FileInfo fi = null;
            try
            {
                fi = new System.IO.FileInfo(fileName);
            }
            catch (ArgumentException) { }
            catch (System.IO.PathTooLongException) { }
            catch (NotSupportedException) { }
            if (ReferenceEquals(fi, null))
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        static void ReadMatchCountDictionary(Dictionary<string, int> dictionary, Excel.Worksheet worksheet)
        {
           // Console.WriteLine("Parsing " + worksheet.Name);
            var initialCount = dictionary.Count;
            var lastRow = worksheet.Cells.SpecialCells(Excel.XlCellType.xlCellTypeLastCell).Row;


            //discover the right columns
            int numMatchPositionColumn = -1;
            int numMatchPositionRow = -1;
            for (int index = 1; index <= lastRow; index++)
            {
                System.Array MyValues = (System.Array)worksheet.get_Range((Char)(64 + 1) + index.ToString(), (Char)(64 + 23) + index.ToString()).Cells.Value;
                for (int column = 1; column < 24; column++)
                {
                    if (MyValues.GetValue(1, column) != null && MyValues.GetValue(1, column).ToString().Equals("NumMatch", StringComparison.OrdinalIgnoreCase))
                    {
                        numMatchPositionColumn = column;
                        numMatchPositionRow = index + 1;
                        break;
                    }
                }
            }

            if (numMatchPositionColumn != -1)
            {
                for (int index = numMatchPositionRow; index <= lastRow; index++)
                {
                    var MyValues = (System.Array)worksheet.get_Range((Char)(64 + numMatchPositionColumn - 1) + index.ToString(), (Char)(64 + numMatchPositionColumn) + index.ToString()).Cells.Value;
                    int matchCount = -1;

                    var v1 = MyValues.GetValue(1, 1);
                    var v2 = MyValues.GetValue(1, 2);

                    var isValid = v1 != null && v1.ToString().Length > 5 && (v1.ToString().Contains('|') || v1.ToString().Contains('_')) &&
                        v2 != null && int.TryParse(v2.ToString(), out matchCount);
                    if (isValid)
                    {
                        var key = v1.ToString().Split('|')[0];
                        if (dictionary.ContainsKey(key))
                        {
                            Console.ForegroundColor = ConsoleColor.DarkYellow;
                            Console.WriteLine("WARNING key already added: " + key);
                            Console.ForegroundColor = ConsoleColor.White;
                        }
                        else
                            dictionary.Add(key, matchCount);
                    }
                }
                Console.WriteLine("This sheet added " + (dictionary.Count - initialCount).ToString() + "/" + dictionary.Count + " new items to the dictionary \n");
            }
            else
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("This sheet was ignored... (no MatchCount column found) \n");
                Console.ForegroundColor = ConsoleColor.White;
            }

        }


        static List<Gene> ReadFileAndSheet(Excel.Worksheet worksheet)
        {
            //Console.WriteLine(worksheet.Name);
            int lastRow;
            try
            {
                //this fails for some worksheets??!
                lastRow = worksheet.Cells.SpecialCells(Excel.XlCellType.xlCellTypeLastCell).Row;
            }
            catch
            {
                lastRow = 5000;
            }
            var result = new List<Gene>();

            for (int index = 2; index <= lastRow; index++)
            {
                System.Array MyValues = (System.Array)worksheet.get_Range("A" + index.ToString(), "C" + index.ToString()).Cells.Value;
                var v1 = MyValues.GetValue(1, 1);
                var v2 = MyValues.GetValue(1, 2);
                var v3 = MyValues.GetValue(1, 3);


                var isValid = v1 != null && v2 != null && v3 != null;
                if (isValid)
                {
                    result.Add(new Gene
                    {
                        SegName = v1.ToString(),
                        SeqLen = v2.ToString(),
                        ITE = v3.ToString()
                    });
                }
            }

            return result;
        }

        static void SaveToWorksheet(List<Gene> list, Dictionary<string, int> dictionary, string sheetName, Excel.Workbook wb)
        {
            Excel.Worksheet ws;            
            //Excel.Worksheet ws = (Excel.Worksheet)wb.Worksheets[1];
            ws = (Excel.Worksheet)wb.Worksheets.Add(After: wb.Sheets[wb.Sheets.Count]);
            ws.Name = sheetName;

            Console.WriteLine("Succesfully matched " + list.Count() + " items from worksheet <" + sheetName + ">\n");


            // Select the Excel cells, in the range c1 to c7 in the worksheet.
            //Excel.Range aRange = ws.get_Range("A1", "C1");
            //Object[] args = new Object[1];
            //args[0] = "TEST";           
            //aRange.GetType().InvokeMember("Value", BindingFlags.SetProperty,null, aRange, args);
            ws.Cells[1, 1] = "SeqName";
            ws.Cells[1, 2] = "SeqLen";
            ws.Cells[1, 3] = "ITE";
            ws.Cells[1, 4] = "NumMatch";
            var i = 2;
            foreach (var gene in list)
            {
                int matchCount = -1;                

                if (dictionary.TryGetValue(gene.SegName.Split('|')[0], out matchCount))
                    gene.MatchCount = matchCount.ToString();
                else
                {
                    if (gene.SegName.Contains('_'))
                    {
                        var parts = gene.SegName.Split('_');
                        if (parts.Length == 2)
                        {
                            var newKeyToTry = parts[0] + '_' + "RS" + parts[1];
                            if (dictionary.TryGetValue(newKeyToTry, out matchCount))
                                gene.MatchCount = matchCount.ToString();
                        }
                    }
                }

                ws.Cells[i, 1] = gene.SegName;
                ws.Cells[i, 2] = gene.SeqLen;
                ws.Cells[i, 3] = gene.ITE;
                ws.Cells[i++, 4] = gene.MatchCount;
            }


            // Fill the cells in the C1 to C7 range of the worksheet with the number 6.
            //Object[] args = new Object[1];
            //args[0] = 6;
            //aRange.GetType().InvokeMember("Value", BindingFlags.SetProperty, null, aRange, args);

            // Change the cells in the C1 to C7 range of the worksheet to the number 8.
            //aRange.Value2 = 8;
        }

        static void SaveToFile(List<Gene> list, Dictionary<string, int> dictionary, string fileName)
        {
            if (File.Exists(fileName))
                try
                {
                    File.Delete(fileName);
                }
                catch
                { }

            Console.Write("Saving matched data " + list.Count() + " items to file:" + (new System.IO.FileInfo(fileName)).Name);
            StreamWriter writetext = new StreamWriter(fileName);
            writetext.WriteLine("SeqName,SeqLen,ITE,NumMatch");
            foreach (var gene in list)
            {
                int matchCount = -1;
                string line = gene.SegName + "," + gene.SeqLen + "," + gene.ITE + ",";

                if (dictionary.TryGetValue(gene.SegName.Split('|')[0], out matchCount))
                    line += matchCount.ToString();
                else
                {
                    if (gene.SegName.Contains('_'))
                    {
                        var parts = gene.SegName.Split('_');
                        if (parts.Length == 2)
                        {
                            var newKeyToTry = parts[0] + '_' + "RS" + parts[1];
                            if (dictionary.TryGetValue(newKeyToTry, out matchCount))
                                line += matchCount.ToString();
                        }
                    }
                }

                if (line != string.Empty)
                    writetext.WriteLine(line);
            }

            writetext.Close();
            Console.Write("Saved !");
        }
    }
}
