using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TestInMemorySorlLike
{
    [Serializable]
    public struct PhotoItem
    {
        public PhotoItem(int id, Int16 collectionId, string quadkey, DateTime dateTime, Hashtable tags, Int16[] involvements)
            : this()
        {
            this.Id = id;
            this.CollectionId = collectionId;
            this.Quadkey = quadkey;
            this.DateTime = dateTime;
            this.Tags = tags;
            this.Involvements = involvements;
        }

        public int Id { private set; get; }
        public Int16 CollectionId { private set; get; }
        public string Quadkey { private set; get; }
        public DateTime DateTime { private set; get; }
        public Hashtable Tags { private set; get; }
        public Int16[] Involvements { private set; get; }

        public override string ToString()
        {
            return string.Format("{0} {1} {2} {3} {4} {5}", this.Id, this.CollectionId, this.DateTime, this.Quadkey, this.Tags, this.Involvements);
        }
    }

    public class DataGenerator
    {
        static public IEnumerable<PhotoItem> Generate(int length)
        {
            var rndm = new Random();
            for (int i = 0; i < length; i++)
            {
                yield return new PhotoItem(i,
                    (short)(i % 19), GenerateQuadkey(20, rndm),
                    DateTime.Now - TimeSpan.FromMinutes(100 + rndm.Next(100000)),
                    GenerateTags(rndm.Next(10), rndm),
                    GenerateInvolvements(rndm.Next(5), rndm));
            }
        }

        static public string GenerateQuadkey(int maxlength, Random rnd)
        {
            var chars = new char[] { '0', '1', '2', '3' };
            var output = new char[maxlength];
            for (int i = 0; i < maxlength; i++)
            {
                output[i] = chars[rnd.Next(4)];
            }
            return new string(output);
        }

        static List<string> words = null;
        static public Hashtable GenerateTags(int maxlength, Random rnd)
        {

            var keys = new string[maxlength];
            for (int i = 0; i < maxlength; i++)
            {
                Char c = (char)(65 + i);
                keys[i] = new string(new Char[] { c });
            }

            var poem = @"Poem, Poems, and Poetic redirect here. For other uses, see Poem (disambiguation), Poems (disambiguation), and Poetic (disambiguation).
        Poetry (from the Greek poiesis — ποίησις — meaning a making, seen also in such terms as hemopoiesis; more narrowly, the making of poetry) is a form of literary art which uses aesthetic and rhythmic[1][2][3] qualities of language—such as phonaesthetics, sound symbolism, and metre—to evoke meanings in addition to, or in place of, the prosaic ostensible meaning.
        Poetry has a long history, dating back to the Sumerian Epic of Gilgamesh. Early poems evolved from folk songs such as the Chinese Shijing, or from a need to retell oral epics, as with the Sanskrit Vedas, Zoroastrian Gathas, and the Homeric epics, the Iliad and the Odyssey. Ancient attempts to define poetry, such as Aristotle's Poetics, focused on the uses of speech in rhetoric, drama, song and comedy. Later attempts concentrated on features such as repetition, verse form and rhyme, and emphasized the aesthetics which distinguish poetry from more objectively-informative, prosaic forms of writing. From the mid-20th century, poetry has sometimes been more generally regarded as a fundamental creative act employing language.
        Poetry uses forms and conventions to suggest differential interpretation to words, or to evoke emotive responses. Devices such as assonance, alliteration, onomatopoeia and rhythm are sometimes used to achieve musical or incantatory effects. The use of ambiguity, symbolism, irony and other stylistic elements of poetic diction often leaves a poem open to multiple interpretations. Similarly, metaphor, simile and metonymy[4] create a resonance between otherwise disparate images—a layering of meanings, forming connections previously not perceived. Kindred forms of resonance may exist, between individual verses, in their patterns of rhyme or rhythm.
        Some poetry types are specific to particular cultures and genres and respond to characteristics of the language in which the poet writes. Readers accustomed to identifying poetry with Dante, Goethe, Mickiewicz and Rumi may think of it as written in lines based on rhyme and regular meter; there are, however, traditions, such as Biblical poetry, that use other means to create rhythm and euphony. Much modern poetry reflects a critique of poetic tradition,[5] playing with and testing, among other things, the principle of euphony itself, sometimes altogether forgoing rhyme or set rhythm.[6][7] In today's increasingly globalized world, poets often adapt forms, styles and techniques from diverse cultures and languages.";
            if (words == null)
                words = poem.Split(' ', ',', '.', ')', '(', '\r', '\n', '[', ']', '—').Where(a => a != "").Distinct().ToList();
            var usedWords = new List<string>(words);

            var output = new Hashtable();
            for (int i = 0; i < maxlength; i++)
            {
                var position = rnd.Next(usedWords.Count);
                var word = usedWords[position];
                usedWords.RemoveAt(position);
                output.Add(keys[i], word);
            }
            return output;
        }

        static public Int16[] GenerateInvolvements(int maxlength, Random rnd)
        {
            var personIndex = new List<Int16>();
            for (short i = 0; i <= 20; i++)
            {
                personIndex.Add(i);
            }

            var output = new List<Int16>();
            for (int i = 0; i < maxlength; i++)
            {
                int position;
                position = rnd.Next(personIndex.Count);
                var index = personIndex[position];
                personIndex.Remove(index);
                output.Add(index);
            }

            return output.ToArray();
        }
    }
}
