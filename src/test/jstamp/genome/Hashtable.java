package jstamp.genome;

public class Hashtable {
    List buckets[];
    int numBucket;
//    int size;
    int resizeRatio;
    int growthFactor;
    
    public Hashtable (int initNumBucket, int resizeRatio, int growthFactor) {
      allocBuckets(initNumBucket);
      numBucket = initNumBucket;
//      size = 0;
      resizeRatio = ((resizeRatio < 0) ? 3 : resizeRatio);
      growthFactor = ((growthFactor < 0) ? 3 : growthFactor);
    }
    
    public boolean TMhashtable_insert (ByteString keyPtr, ByteString dataPtr) {
      int i = keyPtr.hashCode() % numBucket;

      Pair findPair = new Pair();
      findPair.firstPtr = keyPtr;
      Pair pairPtr = buckets[i].find(findPair);
      if (pairPtr != null) {
          return false;
      }

      Pair insertPtr = new Pair(keyPtr, dataPtr);

      /* Add new entry  */
      if (buckets[i].insert(insertPtr) == false) {
          return false;
      }

//      size++;

      return true;
    }
    
    public int size() {
    	int size = 0;
    	for (int i = 0; i < numBucket; i++)
    		size += buckets[i].size();
    	return size;
    }
    
    void allocBuckets (int numBucket) {
      int i;
      /* Allocate bucket: extra bucket is dummy for easier iterator code */
      buckets = new List[numBucket+1];
      
      for (i = 0; i < (numBucket + 1); i++) {
          List chainPtr = new List();
          buckets[i] = chainPtr;
      }
    }
}
