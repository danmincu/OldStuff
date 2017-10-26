// ==++==
// 
//   Copyright (c) Microsoft Corporation.  All rights reserved.
// 
// ==--==
/*============================================================
**
** Class:  Hashtable
**
** <OWNER>[....]</OWNER>
**
**
** Purpose: Hash table implementation
**
** 
===========================================================*/

namespace System.Collections {
    using System;
    using System.Runtime;
    using System.Runtime.Serialization;
    using System.Security.Permissions;
    using System.Diagnostics;
    using System.Threading; 
    using System.Runtime.CompilerServices;
    using System.Runtime.ConstrainedExecution;
    using System.Diagnostics.Contracts;
    using System.Security.Cryptography;
   
    // The Hashtable class represents a dictionary of associated keys and values
    // with constant lookup time.
    // 
    // Objects used as keys in a hashtable must implement the GetHashCode
    // and Equals methods (or they can rely on the default implementations
    // inherited from Object if key equality is simply reference
    // equality). Furthermore, the GetHashCode and Equals methods of
    // a key object must produce the same results given the same parameters for the
    // entire time the key is present in the hashtable. In practical terms, this
    // means that key objects should be immutable, at least for the time they are
    // used as keys in a hashtable.
    // 
    // When entries are added to a hashtable, they are placed into
    // buckets based on the hashcode of their keys. Subsequent lookups of
    // keys will use the hashcode of the keys to only search a particular bucket,
    // thus substantially reducing the number of key comparisons required to find
    // an entry. A hashtable's maximum load factor, which can be specified
    // when the hashtable is instantiated, determines the maximum ratio of
    // hashtable entries to hashtable buckets. Smaller load factors cause faster
    // average lookup times at the cost of increased memory consumption. The
    // default maximum load factor of 1.0 generally provides the best balance
    // between speed and size. As entries are added to a hashtable, the hashtable's
    // actual load factor increases, and when the actual load factor reaches the
    // maximum load factor value, the number of buckets in the hashtable is
    // automatically increased by approximately a factor of two (to be precise, the
    // number of hashtable buckets is increased to the smallest prime number that
    // is larger than twice the current number of hashtable buckets).
    // 
    // Each object provides their own hash function, accessed by calling
    // GetHashCode().  However, one can write their own object 
    // implementing IEqualityComparer and pass it to a constructor on
    // the Hashtable.  That hash function (and the equals method on the 
    // IEqualityComparer) would be used for all objects in the table.
    //
    // Changes since V1 during Whidbey:
    // *) Deprecated IHashCodeProvider, use IEqualityComparer instead.  This will
    //    allow better performance for objects where equality checking can be
    //    done much faster than establishing an ordering between two objects,
    //    such as an ordinal string equality check.
    // 


//    [FriendAccessAllowed]
    internal static class HashHelpers
    {

#if FEATURE_RANDOMIZED_STRING_HASHING
        public const int HashCollisionThreshold = 100;
        public static bool s_UseRandomizedStringHashing = String.UseRandomizedHashing();
#endif

        // Table of prime numbers to use as hash table sizes. 
        // A typical resize algorithm would pick the smallest prime number in this array
        // that is larger than twice the previous capacity. 
        // Suppose our Hashtable currently has capacity x and enough elements are added 
        // such that a resize needs to occur. Resizing first computes 2x then finds the 
        // first prime in the table greater than 2x, i.e. if primes are ordered 
        // p_1, p_2, ..., p_i, ..., it finds p_n such that p_n-1 < 2x < p_n. 
        // Doubling is important for preserving the asymptotic complexity of the 
        // hashtable operations such as add.  Having a prime guarantees that double 
        // hashing does not lead to infinite loops.  IE, your hash function will be 
        // h1(key) + i*h2(key), 0 <= i < size.  h2 and the size must be relatively prime.
        public static readonly int[] primes = {
            3, 7, 11, 17, 23, 29, 37, 47, 59, 71, 89, 107, 131, 163, 197, 239, 293, 353, 431, 521, 631, 761, 919,
            1103, 1327, 1597, 1931, 2333, 2801, 3371, 4049, 4861, 5839, 7013, 8419, 10103, 12143, 14591,
            17519, 21023, 25229, 30293, 36353, 43627, 52361, 62851, 75431, 90523, 108631, 130363, 156437,
            187751, 225307, 270371, 324449, 389357, 467237, 560689, 672827, 807403, 968897, 1162687, 1395263,
            1674319, 2009191, 2411033, 2893249, 3471899, 4166287, 4999559, 5999471, 7199369};

        // Used by Hashtable and Dictionary's SeralizationInfo .ctor's to store the SeralizationInfo
        // object until OnDeserialization is called.
        private static ConditionalWeakTable<object, SerializationInfo> s_SerializationInfoTable;

        internal static ConditionalWeakTable<object, SerializationInfo> SerializationInfoTable 
        {  
            get 
            { 
                if(s_SerializationInfoTable == null)
                {
                    ConditionalWeakTable<object, SerializationInfo> newTable = new ConditionalWeakTable<object, SerializationInfo>();
                    Interlocked.CompareExchange(ref s_SerializationInfoTable, newTable, null);
                }

                return s_SerializationInfoTable;
            }

        }

        [ReliabilityContract(Consistency.WillNotCorruptState, Cer.Success)]
        public static bool IsPrime(int candidate) 
        {
            if ((candidate & 1) != 0) 
            {
                int limit = (int)Math.Sqrt (candidate);
                for (int divisor = 3; divisor <= limit; divisor+=2)
                {
                    if ((candidate % divisor) == 0)
                        return false;
                }
                return true;
            }
            return (candidate == 2);
        }

        [ReliabilityContract(Consistency.WillNotCorruptState, Cer.Success)]
        public static int GetPrime(int min) 
        {
            if (min < 0)
                throw new ArgumentException("Arg_HTCapacityOverflow");
            Contract.EndContractBlock();

            for (int i = 0; i < primes.Length; i++) 
            {
                int prime = primes[i];
                if (prime >= min) return prime;
            }

            //outside of our predefined table. 
            //compute the hard way. 
            for (int i = (min | 1); i < Int32.MaxValue;i+=2) 
            {
                //internal const Int32 HashPrime = 101;

                if (IsPrime(i) && ((i - 1) % /*Hashtable.HashPrime*/ 101 != 0))
                    return i;
            }
            return min;
        }

        public static int GetMinPrime() 
        {
            return primes[0];
        }

        // Returns size of hashtable to grow to.
        public static int ExpandPrime(int oldSize)
        {
            int newSize = 2 * oldSize;

            // Allow the hashtables to grow to maximum possible size (~2G elements) before encoutering capacity overflow.
            // Note that this check works even when _items.Length overflowed thanks to the (uint) cast
            if ((uint)newSize > MaxPrimeArrayLength && MaxPrimeArrayLength > oldSize)
            {
                Contract.Assert( MaxPrimeArrayLength == GetPrime(MaxPrimeArrayLength), "Invalid MaxPrimeArrayLength");
                return MaxPrimeArrayLength;
            }

            return GetPrime(newSize);
        }


        // This is the maximum prime smaller than Array.MaxArrayLength
        public const int MaxPrimeArrayLength = 0x7FEFFFFD;

#if FEATURE_RANDOMIZED_STRING_HASHING
        public static bool IsWellKnownEqualityComparer(object comparer)
        {
            return (comparer == null || comparer == System.Collections.Generic.EqualityComparer<string>.Default || comparer is IWellKnownStringEqualityComparer); 
        }

        public static IEqualityComparer GetRandomizedEqualityComparer(object comparer)
        {
            Contract.Assert(comparer == null || comparer == System.Collections.Generic.EqualityComparer<string>.Default || comparer is IWellKnownStringEqualityComparer); 

            if(comparer == null) {
                return new System.Collections.Generic.RandomizedObjectEqualityComparer();
            } 

            if(comparer == System.Collections.Generic.EqualityComparer<string>.Default) {
                return new System.Collections.Generic.RandomizedStringEqualityComparer();
            }

            IWellKnownStringEqualityComparer cmp = comparer as IWellKnownStringEqualityComparer;

            if(cmp != null) {
                return cmp.GetRandomizedEqualityComparer();
            }

            Contract.Assert(false, "Missing case in GetRandomizedEqualityComparer!");

            return null;
        }

        public static object GetEqualityComparerForSerialization(object comparer)
        {
            if(comparer == null)
            {
                return null;
            }

            IWellKnownStringEqualityComparer cmp = comparer as IWellKnownStringEqualityComparer;

            if(cmp != null)
            {
                return cmp.GetEqualityComparerForSerialization();
            }

            return comparer;
        }
 
        private const int bufferSize = 1024;
        private static RandomNumberGenerator rng;
        private static byte[] data;
        private static int currentIndex = bufferSize;
        private static readonly object lockObj = new Object();

        internal static long GetEntropy() 
        {
            lock(lockObj) {
                long ret;

                if(currentIndex == bufferSize) 
                {
                    if(null == rng)
                    {
                        rng = RandomNumberGenerator.Create();
                        data = new byte[bufferSize];
                        Contract.Assert(bufferSize % 8 == 0, "We increment our current index by 8, so our buffer size must be a multiple of 8");
                    }

                    rng.GetBytes(data);
                    currentIndex = 0;
                }

                ret = BitConverter.ToInt64(data, currentIndex);
                currentIndex += 8;

                return ret;
            }
        }
#endif // FEATURE_RANDOMIZED_STRING_HASHING
    }
}
