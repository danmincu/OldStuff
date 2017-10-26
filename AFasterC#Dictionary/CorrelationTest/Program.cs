using System;
//using System.Collections.Generic;
using System.Collections.Generic.My;
using System.Linq;

namespace CorellationTest
{
    class Program
    {
        static int NumberOfNodes = 24;
        static int RecordsPerSecond = 5000;
        static int RecordTTLsec = 10 /*minutes*/ * 60 /*seconds*/;

        static void Main(string[] args)
        {
            Console.WriteLine(String.Format("Setting up the bolt and its {0} records cache..", (int)(RecordsPerSecond * RecordTTLsec / NumberOfNodes)));
            var b = Setup();
            Console.WriteLine("The bolt was setup. Reset its counters..");
            b.ResetRate();

            //1 million records
            for (int i = 0; i < 1000000; i++)
            {
                b.Go();
                if (i % 10000 == 0)
                    Console.WriteLine(b.Rate().ToString() + " recors per second");
            }
        }


        static Bolt Setup()
        {
            var cacheLength = (int)(RecordsPerSecond * RecordTTLsec / NumberOfNodes);
            var b = new Bolt();
            for (int i = 0; i < cacheLength; i++)
            {
                b.GetCache().Add(Guid.NewGuid(), new Payload());
            }

            return b;
        }
    }


    class Bolt
    {
        Dictionary<Guid, Payload> cache = new Dictionary<Guid, Payload>();
        Int64 internalRateCount;
        Int64 internalTick = DateTime.Now.Ticks;
        Random random = new Random();

        public void ResetRate()
        {
            internalRateCount = 0;
            internalTick = DateTime.Now.Ticks;
        }

        public void Go()
        {
            if (random.Next(10) < 7)
                this.Fail();
            else this.Succeed();
        }

        public int Rate()
        {
            var elapsedSeconds = TimeSpan.FromTicks(DateTime.Now.Ticks - internalTick).TotalSeconds;
            var rate = (int)(internalRateCount / elapsedSeconds);
            ResetRate();
            return rate;
        }

        public Dictionary<Guid, Payload> GetCache()
        {
            return cache;
        }

        public void Succeed()
        {
            internalRateCount++;
            var randomPositionOfSuccessfullKey = random.Next(this.GetCache().Keys.Count);            
            var electedKey = this.GetCache().Keys.ElementAt(randomPositionOfSuccessfullKey);            
            if (electedKey != null)
            {
                Payload payload;
                this.GetCache().Remove(electedKey, out payload);
                //emulate a "merge" of two payloads
                if (payload != null)
                    for (int i = 0; i < payload.Data.Length; i++)
                    {
                        payload.Data[i] = payload.Data[i] + "merged";
                    }
                this.GetCache().Add(Guid.NewGuid(), new Payload());
            }
        }

        public void Fail()
        {
            internalRateCount++;
            var nonExistentKey = Guid.NewGuid();
            if (this.GetCache().ContainsKey(nonExistentKey))
            {
                // big deal
                throw new InvalidOperationException("Your theory is really wrong dude..Or the uniquness of a Guid :)");
            }
            else
            {
                
                GetCache().Remove(GetCache().Keys.First());
                //super fast?
                //GetCache().RemoveFirst();                
                GetCache().Add(nonExistentKey, new Payload());
            }
        }

    }

    class Payload
    {
        static int payloadLength = 10;
        Random random = new Random();
        public Payload()
        {
            this.Data = new string[payloadLength - 1];
            for (int i = 0; i < this.Data.Length; i++)
            {
                this.Data[i] = RandomString(10 + random.Next(30));
            }
            this.DateTime = DateTime.Now;
        }

        DateTime DateTime { set; get; }
        public string[] Data { set; get; }

        public static string RandomString(int length)
        {
            const string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            var random = new Random();
            return new string(Enumerable.Repeat(chars, length)
              .Select(s => s[random.Next(s.Length)]).ToArray());
        }
    }

}

