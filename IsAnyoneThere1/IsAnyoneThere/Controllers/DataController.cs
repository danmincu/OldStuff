using IsAnyoneThere.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace IsAnyoneThere.Controllers
{
    public class DataController : ApiController
    {
        [Route("GetGraphData")]
        public IHttpActionResult GetGraphData()
        {
            var ip = this.Request.GetClientIpAddress();
            var data = Rootobject.TestData();
            if (data == null)
            {
                return NotFound(); // Returns a NotFoundResult
            }
            return Ok(data);  // Returns an OkNegotiatedContentResult
        }


        [HttpPost]
        [Route("PutData")]
        public HttpResponseMessage Save([FromBody] Nestatroot netstat)
        {
            // I am just returning the posted model as it is. 
            // You may do other stuff and return different response.
            // Ex : missileService.LaunchMissile(m);
            return new HttpResponseMessage(HttpStatusCode.OK);
        }


    }

    public static class HttpRequestMessageExtensions
    {
        private const string HttpContext = "MS_HttpContext";
        private const string RemoteEndpointMessage = "System.ServiceModel.Channels.RemoteEndpointMessageProperty";

        public static string GetClientIpAddress(this HttpRequestMessage request)
        {
            if (request.Properties.ContainsKey(HttpContext))
            {
                dynamic ctx = request.Properties[HttpContext];
                if (ctx != null)
                {
                    return ctx.Request.UserHostAddress;
                }
            }

            if (request.Properties.ContainsKey(RemoteEndpointMessage))
            {
                dynamic remoteEndpoint = request.Properties[RemoteEndpointMessage];
                if (remoteEndpoint != null)
                {
                    return remoteEndpoint.Address;
                }
            }

            return null;
        }
    }

}
