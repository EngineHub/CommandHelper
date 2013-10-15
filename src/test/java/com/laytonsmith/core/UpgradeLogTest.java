package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 */
public class UpgradeLogTest {

	public UpgradeLogTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
	
	@Test
	public void testOldTasksArentRun() throws Exception {
		File log = new File("upgradeLogTestOldTasksArentRun");
		FileUtil.write("[{\"upgradeTime\":\"1234\",\"breadcrumb\":\"task\"}]", log);
		try{
			UpgradeLog l = new UpgradeLog(log);
			final AtomicInteger i = new AtomicInteger(0);
			l.addUpgradeTask(new UpgradeLog.UpgradeTask() {

				@Override
				public boolean doRun() {
					return false;
				}

				@Override
				public void run() {
					fail("This task should not run");
				}
			});
			l.addUpgradeTask(new UpgradeLog.UpgradeTask() {

				@Override
				public boolean doRun() {
					return !hasBreadcrumb("task");
				}

				@Override
				public void run() {
					fail("This task should not run");
				}
			});
			l.addUpgradeTask(new UpgradeLog.UpgradeTask() {

				@Override
				public boolean doRun() {
					return hasBreadcrumb("task");
				}

				@Override
				public void run() {
					i.incrementAndGet();
				}
			});
			l.addUpgradeTask(new UpgradeLog.UpgradeTask() {

				@Override
				public boolean doRun() {
					return true;
				}

				@Override
				public void run() {
					if(i.get() != 1){
						fail("This task isn't sequential");
					}
					i.incrementAndGet();
				}
			});
			l.addUpgradeTask(new UpgradeLog.UpgradeTask() {

				@Override
				public boolean doRun() {
					return true;
				}

				@Override
				public void run() {
					leaveBreadcrumb("task2");
				}
			});
			l.runTasks();
			assertTrue(FileUtil.read(log).contains("task2"));
			assertEquals(2, i.get());
		} finally {
			log.delete();
		}
	}

}
