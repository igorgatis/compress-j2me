package com.googlecode.j2me.compress;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ LZWHashTest.class, LZWDictTest.class,
    LZWStreamTest.class, LZWOutputStreamTest.class, LZWInputStreamTest.class,
    LZWStreamE2ETest.class, LZWIOStreamsE2ETest.class })
public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllTests.class.getName());
    //$JUnit-BEGIN$

    //$JUnit-END$
    return suite;
  }

}
