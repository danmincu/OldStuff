using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace IsAnyoneThere.Models
{


    // {
    //    "nestat": [
    //      { "remoteIp": "192.12.23.4", "port": 2314 },
	//      { "remoteIp": "19.12.23.4", "port": 1214 }
    //    ]
    // }

    public class Nestatroot
    {
        public Nestat[] nestat { get; set; }
    }

    public class Nestat
    {
        public string remoteIp { get; set; }
        public int port { get; set; }
    }
}
