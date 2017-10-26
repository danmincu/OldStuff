using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Data.Entity;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CodeToDatabase
{

    // http://msdn.microsoft.com/en-ca/data/jj193542.aspx
    class Program
    {
        static void Main(string[] args)
        {
            using (var context = new ImageContex())
            {

                var tags = new Dictionary<string, string>();
                tags.Add("A","B");
                tags.Add("C","D");
                tags.Add("E","F");

                var props = new Dictionary<string, string>();
                props.Add("m","1");
                props.Add("n","2");
                props.Add("o","3");

                var output = Serializer.ToJson(props);
                var list = Serializer.FromJson(output);

                var image = new ImageMetadata() {
                Id = Guid.NewGuid(),
                DateTime = DateTime.Now,
                FileDateTime = DateTime.Now + TimeSpan.FromDays(1),
                ImportDateTime = DateTime.UtcNow,
                Latitude = 45,
                Longitude = -75,                
                Tags = tags,
                CollectionId = Guid.NewGuid(),
                StorageInfo = new RemoteStorageInfo() {Type="Flickr",Version="123", Properties= props}
                };
                context.Images.Add(image);
                context.SaveChanges();
                var retrieved = context.Images.Where(m => m.Id == image.Id).FirstOrDefault();
                Console.WriteLine(retrieved.Id);
            }

        }
    }

    static class Serializer
    {

        public static string ToJson(Dictionary<string, string> input)
        {
            string json = JsonConvert.SerializeObject(input, Formatting.Indented);
            return json;
        }

        public static Dictionary<string, string> FromJson(string xml)
        {
            return JsonConvert.DeserializeObject<Dictionary<string, string>>(xml);
        }    
    }


    public class ImageMetadata
    {
        [Key]
        public Guid Id { set; get; }

        public Guid CollectionId { set; get; }

        public DateTime? DateTime { set; get; }

        public DateTime ImportDateTime { set; get; }

        public DateTime? FileDateTime { set; get; }

        public DateTime BestDate
        {
            get
            {
                return DateTime ?? (FileDateTime ?? ImportDateTime);
            }
        }

        [NotMapped]
        public Dictionary<string, string> Tags { set; get; }

        public string TagsAsJson
        {
            get
            {
                return Serializer.ToJson(this.Tags);
            }
            set
            {
                this.Tags = Serializer.FromJson(value);
            }
        }

        public string Quadkey
        {
            get
            {
                if (this.Coordinate == null) return null;
                return this.Coordinate.Value.Latitude.ToString();
            }
        }

        [NotMapped]
        public Coordinate? Coordinate
        {
            get
            {
                if (this.Latitude == null || this.Longitude == null)
                    return null;
                else
                    return new Coordinate((double)this.Latitude, (double)this.Longitude);
            }
        }

        public double? Latitude
        {
            set;
            get;
        }

        public double? Longitude
        {
            set;
            get;
        }



        public byte[] Thumb { set; get; }

        public RemoteStorageInfo StorageInfo { set; get; }
    }

    public class RemoteStorageInfo
    {
        public string Type { set; get; }
        public string Version { set; get; }

        [NotMapped]
        public Dictionary<string, string> Properties { set; get; }
                
        public string PropertiesAsJson
        {
            get
            {
                return Serializer.ToJson(this.Properties);
            }
            set
            {
                this.Properties = Serializer.FromJson(value);
            }
        }
    }

    public struct Coordinate
    {
        private const double EndOfTheWorldLongitude = 180;

        private readonly double latitude;
        public double Latitude
        {
            get
            {
                return this.latitude;
            }
        }

        private readonly double longitude;
        public double Longitude
        {
            get
            {
                return this.longitude;
            }
        }

        public Coordinate(double latitude, double longitude)
        {
            if (latitude < -90F || latitude > 90F) throw new ArgumentException("Latitude must be between -90 and 90", "latitude");
            if (longitude < -180F || longitude > 180F) throw new ArgumentException("Longitude must be between -180 and 180", "longitude");

            this.latitude = latitude;
            this.longitude = longitude;

        }

    }

    public class ImageContex : DbContext
    {
        public DbSet<ImageMetadata> Images { set; get; }
    }

}