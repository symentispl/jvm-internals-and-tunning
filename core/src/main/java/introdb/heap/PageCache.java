package introdb.heap;

import static java.lang.Integer.valueOf;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class PageCache {

  private final Map<Integer, SoftReference<ByteBuffer>> pages;
  private final Function<Integer, ByteBuffer> pageLoader;

  PageCache(int maxPageNr, Function<Integer, ByteBuffer> pageLoader) {
    this.pageLoader = pageLoader;
    this.pages = new HashMap<>(maxPageNr);
  }

  /**
   * Gets a page, or if it doesn't exists, tries to load it from disk.
   * 
   * @param pageNr page number
   * @return null when there is no such page in file
   */
  ByteBuffer get(Integer pageNr) {
    var reference = pages.get(valueOf(pageNr));
    if (reference != null) {
      var page = reference.get();
      if (page != null) {
        return page;
      }
    }
    ByteBuffer byteBuffer = pageLoader.apply(pageNr);
    if (byteBuffer != null) {
      pages.put(pageNr, new SoftReference<ByteBuffer>(byteBuffer));
    }
    return byteBuffer;
  }

  /**
   * Puts page to page cache (but doesn't flush it), it throws exception if same
   * page already exists, but wrapped in different ByteBuffer.
   * 
   * @param pageNr page number
   * @param page physical record (page)
   * @returns page written to cache
   */
  void put(Integer pageNr, ByteBuffer page) {
    var ref = pages.get(pageNr);
    if (ref == null || ref.get() == null) {
      pages.put(pageNr, new SoftReference<ByteBuffer>(page));
    } else {
      throw new IllegalStateException("page already in cache, something went terribly wrong");
    }
  }

  void remove(Integer pageNr) {
    pages.remove(pageNr);
  }

}
