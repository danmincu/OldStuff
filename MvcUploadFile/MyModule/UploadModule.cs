using System;
using System.Collections.Generic;
using System.Text;
using System.Web;
using System.IO;
using System.Web.UI;
using System.Web.UI.WebControls;

namespace MyModule
{
    public class UploadModule : IHttpModule
    {
        public void Init(HttpApplication app)
        {
            // app.BeginRequest += new EventHandler(app_BeginRequest);
            app.BeginRequest += new EventHandler(Application_BeginRequest);

        }

        private void Application_BeginRequest(object theSender, EventArgs theE)
        {
            HttpApplication httpApp = theSender as HttpApplication;
            HttpContext context = ((HttpApplication)theSender).Context;
            IServiceProvider provider = (IServiceProvider)context;
            HttpWorkerRequest httpWorkerReq = (HttpWorkerRequest)provider.GetService(typeof(HttpWorkerRequest));
            long receivedBytes = 0;
            long initialBytes = 0;
            byte[] buffer = new byte[10 * 1024 * 1024];

            if (httpApp.Request.HttpMethod == "POST")
            {
                // get the total body length
                UInt32 requestLength = (UInt32)httpWorkerReq.GetTotalEntityBodyLength();
                // Get the initial bytes loaded            
                if (httpWorkerReq.GetPreloadedEntityBody() != null)
                    receivedBytes = httpWorkerReq.GetPreloadedEntityBody().Length;
                if (!httpWorkerReq.IsEntireEntityBodyIsPreloaded())
                {
                    // Set the received bytes to initial bytes before start reading
                    do
                    {
                        // Read another set of bytes
                        initialBytes = httpWorkerReq.ReadEntityBody(buffer, buffer.Length);
                        // Update the received bytes
                        receivedBytes += initialBytes;
                        System.Diagnostics.Trace.WriteLine("#bytes read: " + receivedBytes.ToString());
                    }
                    while (initialBytes > 0);
                }
                System.Diagnostics.Trace.WriteLine("Request Length=" + requestLength.ToString() + " Total bytes read=" + receivedBytes.ToString());
            }
        }


        void app_BeginRequest(object sender, EventArgs e)
        {
            HttpContext context = ((HttpApplication)sender).Context;

            if (context.Request.ContentLength > 4096000)
            {
                IServiceProvider provider = (IServiceProvider)context;
                HttpWorkerRequest wr = (HttpWorkerRequest)provider.GetService(typeof(HttpWorkerRequest));
                FileStream fs = null;
                // Check if body contains data
                if (wr.HasEntityBody())
                {
                    // get the total body length
                    int requestLength = wr.GetTotalEntityBodyLength();
                    // Get the initial bytes loaded
                    int initialBytes = wr.GetPreloadedEntityBody().Length;

                    if (!wr.IsEntireEntityBodyIsPreloaded())
                    {
                        byte[] buffer = new byte[512000];
                        string[] fileName = context.Request.QueryString["fileName"].Split(new char[] { '\\' });
                        fs = new FileStream(context.Server.MapPath("~/Uploads/" + fileName[fileName.Length - 1]), FileMode.CreateNew);
                        // Set the received bytes to initial bytes before start reading
                        int receivedBytes = initialBytes;
                        while (requestLength - receivedBytes >= initialBytes)
                        {
                            // Read another set of bytes
                            initialBytes = wr.ReadEntityBody(buffer, buffer.Length);
                            // Write the chunks to the physical file
                            fs.Write(buffer, 0, buffer.Length);
                            // Update the received bytes
                            receivedBytes += initialBytes;
                        }
                        initialBytes = wr.ReadEntityBody(buffer, requestLength - receivedBytes);

                    }
                }
                fs.Flush();
                fs.Close();
                context.Response.Redirect("UploadFinished.aspx");
            }

        }
        public void Dispose()
        {
        }
    }
}