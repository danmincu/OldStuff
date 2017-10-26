using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace TestInMemorySorlLike
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            data = DataGenerator.Generate(1000000).ToArray();
            var s = this.Search();
            this.Title = s;
            
        }

        private IEnumerable<PhotoItem> data;
        public string Search()
        {
            return data.Where(a => a.Id == 50000).Select(a => a.Quadkey).FirstOrDefault().ToString();
        }

        private void Serialize()
        {
            var intermediateData = new List<List<PhotoItem>>();

            var i = 0;
            while( 100000 * i < this.data.Count())
            {
                intermediateData.Add( new List<PhotoItem>(this.data.Skip(100000 * i).Take(100000)));
                i++;
            }


            // In this case, use a file stream.
            FileStream fs = new FileStream(@"DataFile.dat", FileMode.Create);

            // Construct a BinaryFormatter and use it to serialize the data to the stream.
            BinaryFormatter formatter = new BinaryFormatter();
            try
            {
                //foreach (var item in this.data)
                //{
                //    formatter.Serialize(fs, item);    
                //}
                //formatter.Serialize(fs, this.data);    
                foreach (var item in intermediateData)
                {
                    formatter.Serialize(fs, item);
                }
            }
            catch (SerializationException e)
            {
                Console.WriteLine("Failed to serialize. Reason: " + e.Message);
                throw;
            }
            finally
            {
                fs.Close();
            }
        
        }


        private void Deserialize()
        {
            // In this case, use a file stream.
            FileStream fs = new FileStream(@"DataFile.dat", FileMode.Open);

            // Construct a BinaryFormatter and use it to serialize the data to the stream.
            BinaryFormatter formatter = new BinaryFormatter();
            try
            {
                var result = new List<List<PhotoItem>>();
                //foreach (var item in this.data)
                //{
                //    formatter.Serialize(fs, item);    
                //}
                //this.data = (List<PhotoItem>)formatter.Deserialize(fs);
                object obj;
                do
                {
                    obj = formatter.Deserialize(fs);
                    var a = obj as List<PhotoItem>;
                    result.Add(a);
                }
                while (fs.Position < fs.Length);

                
                //var obj1 = formatter.Deserialize(fs);
                
                //var c = obj as PhotoItem[];
                //var b = a[100000 - 1];
            }
            catch (SerializationException e)
            {
                Console.WriteLine("Failed to serialize. Reason: " + e.Message);
                throw;
            }
            finally
            {
                fs.Close();
            }

        }



        private void Button_Click(object sender, RoutedEventArgs e)
        {
            Deserialize();
            //Serialize();
            return;






            this.resultText.Text = "";
            //quadkey search
            //var before = DateTime.Now.Ticks;
            //var list = data.Where(a => a.Quadkey.Contains("01230"));
            //this.resultText.Text = list.Count().ToString() + " " + list.OrderByDescending(a => a.Id).FirstOrDefault().ToString();
            //var after = TimeSpan.FromTicks(DateTime.Now.Ticks - before).TotalMilliseconds;
            //this.logText.Text += ((string.IsNullOrEmpty(this.logText.Text)) ? "" : "\r\n") + after + " ms";

            var tagCount = new Dictionary<string, int>(); 
            var before = DateTime.Now.Ticks;
            
            foreach (var item in data)
            {
                foreach (string term in item.Tags.Values)
                {
                    if (tagCount.ContainsKey(term))
                        tagCount[term] += 1;
                    else
                        tagCount.Add(term, 1);                    
                }    
            }
            
            foreach (var pair in tagCount.OrderByDescending(t => t.Value))
            {
                this.resultText.Text += string.Format("{0}({1})", pair.Key,pair.Value);
            }
            
            
            var after = TimeSpan.FromTicks(DateTime.Now.Ticks - before).TotalMilliseconds;
            this.logText.Text += ((string.IsNullOrEmpty(this.logText.Text)) ? "" : "\r\n") + after + " ms";

        }
    }


}
