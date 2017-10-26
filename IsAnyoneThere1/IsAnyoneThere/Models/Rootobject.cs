using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace IsAnyoneThere.Models
{
    public class Rootobject
    {
        static string graph = @"{
        ""nodes"": [
          { ""name"": ""Dinobot"", ""group"": 0, ""isSystem"": true},
          { ""name"": ""Appserver"", ""group"": 1 },
          { ""name"": ""39 Database"", ""group"": 1 },
          { ""name"": ""Site Manager"", ""group"": 1 },
          { ""name"": ""Solr 1"", ""group"": 1 },
          { ""name"": ""Solr 2"", ""group"": 1 },
          { ""name"": ""Solr 3"", ""group"": 1 },
          { ""name"": ""Kafka 1"", ""group"": 1 },
          { ""name"": ""Kafka 2"", ""group"": 1 },
          { ""name"": ""Kafka 3"", ""group"": 1 },
          { ""name"": ""Dmincu-T3600"", ""group"": 10 },
          { ""name"": ""UBhide-T3500"", ""group"": 5 },
          { ""name"": ""EPaterson-T3610"", ""group"": 10 },
          { ""name"": ""Stability"", ""group"": 0, ""isSystem"":true },
          { ""name"": ""Y"", ""group"": 2 },
          { ""name"": ""X"", ""group"": 2 },
          { ""name"": ""XX"", ""group"": 2 },
          { ""name"": ""YY"", ""group"": 2 }
        ],
        ""links"": [
          { ""source"": 1, ""target"": 0, ""value"": 1 },
          { ""source"": 2, ""target"": 0, ""value"": 8 },
          { ""source"": 3, ""target"": 0, ""value"": 10 },
          { ""source"": 3, ""target"": 2, ""value"": 6 },
          { ""source"": 4, ""target"": 0, ""value"": 1 },
          { ""source"": 5, ""target"": 0, ""value"": 1 },
          { ""source"": 6, ""target"": 0, ""value"": 1 },
          { ""source"": 7, ""target"": 0, ""value"": 1 },
          { ""source"": 8, ""target"": 0, ""value"": 2 },
          { ""source"": 9, ""target"": 0, ""value"": 1 },
          { ""source"": 11, ""target"": 10, ""value"": 1 },
          { ""source"": 11, ""target"": 3, ""value"": 3 },
          { ""source"": 11, ""target"": 2, ""value"": 3 },
          { ""source"": 11, ""target"": 0, ""value"": 5 },
          { ""source"": 12, ""target"": 11, ""value"": 1 },

          { ""source"": 13, ""target"": 14, ""value"": 1 },
          { ""source"": 14, ""target"": 15, ""value"": 1 },
          { ""source"": 15, ""target"": 13, ""value"": 1 },
          { ""source"": 13, ""target"": 16, ""value"": 1 },
          { ""source"": 13, ""target"": 17, ""value"": 1 }       


        ]
    }";

        public static Rootobject TestData()
        {
            return new System.Web.Script.Serialization.JavaScriptSerializer().Deserialize<Rootobject>(Rootobject.graph);
        }

        public Node[] nodes { get; set; }
        public Link[] links { get; set; }
    }

    public class Node
    {
        public string name { get; set; }
        public int group { get; set; }
        public bool isSystem { get; set; }
    }

    public class Link
    {
        public int source { get; set; }
        public int target { get; set; }
        public int value { get; set; }
    }
}
