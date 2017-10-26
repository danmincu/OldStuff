using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using C1.WPF.Maps;
using System.Collections;
using System.Xml.Serialization;
using System.Reflection;
using System.ComponentModel;
using System.Windows.Threading;
using System.Threading;

namespace MapsSamples
{
    public partial class Airplanes : UserControl
    {
        const double minStoreZoom = 5.5;

        DataBase database;


        public Airplanes()
        {
            InitializeComponent();
            LoadDataBase();
            InitializeMapLayers();
        }



        private void LoadDataBase()
        {
            Random r = new Random();
            this.database = new DataBase();

            for (int i = 0; i < 5; i++)
            {                
                this.database.Airplanes.Add(new Airplane() { Name = "Airplane " + i , Latitude = 45.115701319438153 + r.NextDouble() * 1.25, Longitude = -75.930482046910969 + r.NextDouble() * 1.25, Bearing = r.Next(0, 180) });
            }
        }

        public void InitializeMapLayers()
        {

            int storeSlices = (int)Math.Pow(2, minStoreZoom);
            var itemTemplate = (DataTemplate)Resources["airplaneTemplate"];
            map.Layers.Add(new C1MapVirtualLayer
            {
                Slices = {
                    new MapSlice(1, 1, 0),
                    new MapSlice(storeSlices, storeSlices, minStoreZoom)
                },
                MapItemsSource = new LocalTrafficSource(database),
                ItemTemplate = (DataTemplate)Resources["airplaneTemplate"]
            });

        }


        public class LocalTrafficSource : IMapVirtualSource
        {
            DataBase _dataBase;

            public LocalTrafficSource(DataBase localDataBase)
            {
                _dataBase = localDataBase;
            }

            public void Request(double minZoom, double maxZoom, Point lowerLeft, Point upperRight, Action<ICollection> callback)
            {
                if (minZoom < minStoreZoom)
                    return;

                var airplanes = new List<Airplane>();

                foreach (var airplane in _dataBase.Airplanes)
                {
                    if (airplane.Latitude > lowerLeft.Y
                      && airplane.Longitude > lowerLeft.X
                      && airplane.Latitude <= upperRight.Y
                      && airplane.Longitude <= upperRight.X)
                    {
                        airplanes.Add(airplane);
                    }
                }

                callback(airplanes);
            }
        }

        private void Image_MouseDown(object sender, MouseButtonEventArgs e)
        {
            if (sender is System.Windows.Controls.Grid)
            {
                var g = sender as System.Windows.Controls.Grid;
                if (g.DataContext is Airplane)
                    ((Airplane)g.DataContext).NewDestination();
            }

            var m = sender;
        }
    }

    public class Entity : INotifyPropertyChanged
    {
        public string Name { get; set; }
        private double latitude, longitude;
        public double Latitude
        {
            get { return latitude; }
            set
            {
                this.latitude = value;
                OnPropertyChanged("Position");
            }
        }
        public double Longitude
        {
            get { return longitude; }
            set
            {
                this.longitude = value;
                OnPropertyChanged("Position");
            }
        }
        public Point Position { get { return new Point(Longitude, Latitude); } }

        public event PropertyChangedEventHandler PropertyChanged;

        // Create the OnPropertyChanged method to raise the event
        protected void OnPropertyChanged(string name)
        {
            PropertyChangedEventHandler handler = PropertyChanged;
            if (handler != null)
            {
                handler(this, new PropertyChangedEventArgs(name));
            }
        }
    }

    public class Airplane : Entity
    {

        const double degreeToRad = Math.PI / 180.0;

        public Airplane()
        {
        
        }

        bool isInitialized = false;
        private void Initialize()
        {
            if (isInitialized)
                return;
            isInitialized = true;
            NewDestination();
            

        }
               
        public List<string> Characteristics { get; set; }

        private double bearing;
        public double Bearing
        {
            set
            {
                this.bearing = value;
                OnPropertyChanged("Bearing");
            }
            get { return bearing; }
        }

        private double destinationLatitude;
        private double destinationLongitude;

        private double stepLatitude;
        private double stepLongitude;


        public void NewDestination()
        {
            Random r = new Random();
            Thread.Sleep(10);            
            this.destinationLatitude = Latitude -0.25 + r.NextDouble() * 0.5;
            this.destinationLongitude = Longitude - 0.25 + r.NextDouble() * 0.5;
            var distanceInKm = ComputeDistance(this.Latitude, this.Longitude, this.destinationLatitude, this.destinationLongitude) / 1000;
            stepLatitude = 0.05 * (destinationLatitude - Latitude) / distanceInKm;
            stepLongitude = 0.05 * (destinationLongitude - Longitude) / distanceInKm;
            Bearing = ComputeBearing(this.Latitude, this.Longitude, this.destinationLatitude, this.destinationLongitude);

        }

        private double ComputeDistance(double lat1, double long1, double lat2, double long2)
        {
            lat1 *= degreeToRad;
            lat2 *= degreeToRad;
            long1 *= degreeToRad;
            long2 *= degreeToRad;
            var deltaLong = long2 - long1;

            double dist = Math.Sin(lat1) * Math.Sin(lat2) + Math.Cos(lat1) * Math.Cos(lat2) * Math.Cos(-deltaLong);
            return (Math.Acos(dist) / degreeToRad) * 60 * 1.1515 * 1.609344 * 1000;
        }
        
        //http://www.igismap.com/formula-to-find-bearing-or-heading-angle-between-two-points-latitude-longitude/
        private double ComputeBearing(double lat1, double long1, double lat2, double long2)
        {
            lat1 *= degreeToRad;
            lat2 *= degreeToRad;
            long1 *= degreeToRad;
            long2 *= degreeToRad;
            var deltaLong = long2 - long1;

            double dist = Math.Sin(lat1) * Math.Sin(lat2) + Math.Cos(lat1) * Math.Cos(lat2) * Math.Cos(-deltaLong);
            dist = (Math.Acos(dist) / degreeToRad) * 60 * 1.1515 * 1.609344 * 1000;

            var x = Math.Cos(lat2) * Math.Sin(deltaLong);
            var y = Math.Cos(lat1) * Math.Sin(lat2) - Math.Sin(lat1) * Math.Cos(lat2) * Math.Cos(deltaLong);
            return Math.Atan2(x, y) / degreeToRad;
        }



        public void FlyOneStep()
        {

           // var b = ComputeBearing(39.099912, -94.581213, 38.627089, -90.200203);

            Initialize();

            var distanceInKm = ComputeDistance(this.Latitude, this.Longitude, this.destinationLatitude, this.destinationLongitude) / 1000;

            if (distanceInKm < 2)
            {
                NewDestination();
            }
            else
            {
                this.Latitude += stepLatitude;
                this.Longitude += stepLongitude;
            }
        }

    }

    public class DataBase
    {
        public List<Airplane> Airplanes {
            get;
            set;
        }
        public double SomeDistance { get; set; }

        public DataBase()
        {           
            Airplanes = new List<Airplane>();
            SomeDistance = 100000;
            DispatcherTimer t = new DispatcherTimer(TimeSpan.FromMilliseconds(40), DispatcherPriority.ApplicationIdle, (o, e) => {
                this.Airplanes.ForEach(a => a.FlyOneStep());
            }, Application.Current.Dispatcher);
            t.Start();
        }
    }

}
