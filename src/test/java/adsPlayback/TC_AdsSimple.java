package adsPlayback;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import baseClass.TestBase;
import config.ConfigReader;
import config.Log;
// import pagesSunShorts.HomePageSunShorts;
// import pagesSunShorts.PlayerSunShorts;
import pagesSunnxt.AdsCheckPage;
import pagesSunnxt.ContentDetailPage;
import pagesSunnxt.ContentLanguageSelectionPage;
import pagesSunnxt.HomePage;
import pagesSunnxt.LoginPage;
import pagesSunnxt.ProfilePage;
import reporting.ExtentReportManager;

/**
 * One simple flow, repeated for every subscription plan:
 * Login -> Movies (pre-roll, mid-roll) -> TV Shows (pre-roll, mid-roll). Sun Shorts is temporarily commented out.
 */
public class TC_AdsSimple extends TestBase {

	private static final Logger logger = Log.getLogger(TC_AdsSimple.class);

	@BeforeMethod
	public void setup(Method method) {
		test = ExtentReportManager.createTest(method.getName());
	}

	@Test(enabled = true)
	public void adsForAllPlansTest() throws InterruptedException {

		int longWait = 90;          // seconds to look for pre-roll / mid-roll / 6th short ad
		// int shortWait = 10;      // seconds to look for an ad on shorts 1 to 5
		// int shortsAdEpisode = 6; // the ad should come on this short

		// Plan name | username key | password key | ads expected? | can play Movies/TV?
		// Keys are read from the environment first, then from config.properties / ads-users.local.properties
		String[][] plans = {
				{"Combo Quarterly Platinum", "test.platinumCombo",  "test.password1","YES", "YES"},
				{"SN Quarterly Lite",        "test.liteSN",      "test.litePassword",      "YES", "YES"},
				{"SNS Quarterly Lite",       "SUNNXT_ENTITLEMENT_SNS_LITE_CREDENTIAL_MOBILE",     "SUNNXT_ENTITLEMENT_SNS_LITE_CREDENTIAL_PASSWORD",     "YES", "NO"},
				{"SNS Annual Premium",       "SUNNXT_ENTITLEMENT_SNS_PREMIUM_CREDENTIAL_MOBILE",  "SUNNXT_ENTITLEMENT_SNS_PREMIUM_CREDENTIAL_PASSWORD",  "NO",  "NO"}
		};
		String[] sections = {
				"TV Shows" // Debug TV Shows alone.
		};
		List<String> failures = new ArrayList<>();

		// ==================== LOOP 1: EVERY PLAN ====================

		for (int i = 0; i < plans.length; i++) {
			String plan = plans[i][0];
			String username = ConfigReader.getRuntime(plans[i][1]);
			String password = ConfigReader.getRuntime(plans[i][2]);
			boolean expectsAds = plans[i][3].equals("YES");
			boolean canPlayVod = plans[i][4].equals("YES");

			if (username == null || username.isBlank() || password == null || password.isBlank()) {
				test.info(plan + " - no credentials found, plan skipped");
				continue;
			}

			test.info("Checking plan: " + plan);
			logger.info("Checking plan: {}", plan);

			try {
				setupDriver(true);
				networkUtils.enableDebugLog("TC_AdsSimple-" + plan);
				launchSunnxt();

				// ---------- Login ----------
				ContentLanguageSelectionPage cls = new ContentLanguageSelectionPage(driver);
				cls.selectSNLanguages("tamil");
				cls.proceedToSNSScreen();
				cls.selectSNSLanguages("tamil");
				cls.clickProceed();

				HomePage hp = new HomePage(driver);
				hp.navigateToLogin();

				LoginPage lp = new LoginPage(driver);
				lp.enterLoginId(username);
				lp.clickContinueButton();

				lp.waitForPasswordScreen();
				lp.enterPassword(password);
				lp.clickLoginButton();
				lp.handleDeviceLimit();

				ProfilePage profile = new ProfilePage(driver);
				test.info("Total profiles: " + profile.getProfileCount());
				profile.selectPrimaryProfile();
				waitForLoginSuccess();
				test.info(plan + " - logged in");

				String homeUrl = driver.getCurrentUrl();
				AdsCheckPage ads = new AdsCheckPage(driver, networkUtils);
				ContentDetailPage cdp = new ContentDetailPage(driver);

				// ==================== LOOP 2: MOVIES AND TV SHOWS ====================

				if (canPlayVod) {
					for (int s = 0; s < sections.length; s++) {
						String section = sections[s];
						int midRollAt;
						String midRollTime;
						if (section.equals("Movies")) {
							midRollAt = 1800;
							midRollTime = "30:00";
						} else {
							midRollAt = 900;
							midRollTime = "15:00";
						}

						try {
							boolean preRollSeen = false;
							boolean longEnough = false;

							// TV Shows uses one fixed title for debugging. Movies keeps random selection.
							for (int attempt = 1; attempt <= 3; attempt++) {
								driver.get(homeUrl);
								if (section.equals("Movies")) {
									hp.navigateToMovies();
									hp.contentPlayBackCheck(-1, -1);
								} else {
									hp.navigateToTvShows();
									hp.selectContentWithoutScrolling(0, 0);
								}
								test.info(plan + " - " + section + " attempt " + attempt + ": " + cdp.getContentName());

								ads.clearAdRequests();              // forget old requests, then press play
								cdp.clickPlayButton();

								// Pre-roll: ad visible, else ad request
								preRollSeen = ads.waitForAd(longWait);
								if (!preRollSeen) {
									preRollSeen = ads.isAdRequested();
									test.info(plan + " - " + section + " pre-roll ad not visible, ad request found = " + preRollSeen);
								}

								if (!ads.waitForContent(120)) {
									throw new IllegalStateException(section + " content did not start playing");
								}
								if (ads.getContentDuration() > midRollAt) {
									longEnough = true;
									break;
								}
								if (section.equals("TV Shows")) {
									test.info("Fixed TV Shows title is too short for the 15:00 mid-roll check");
									break;
								}
								test.info(plan + " - " + section + " title does not exceed " + midRollTime + ", trying another one");
							}

							// Pre-roll result
							String preRollResult = plan + " - " + section + " pre-roll: ad seen = " + preRollSeen + ", ad expected = " + expectsAds;
							logger.info(preRollResult);
							if (preRollSeen == expectsAds) {
								test.pass(preRollResult);
							} else {
								test.fail(preRollResult);
								failures.add(preRollResult);
							}

							// Mid-roll
							if (longEnough) {
								ads.clearAdRequests();
								if (Boolean.getBoolean("ads.seek.debug")) {
									ads.startSeekDebug();
								}
								logger.info("{} - {} mid-roll: seeking to {} ({} seconds)", plan, section, midRollTime, midRollAt);
								ads.seekTo(midRollAt);

								// Mid-roll: ad visible, else ad request
								boolean midRollVisible = ads.waitForAd(longWait);
								if (!ads.waitForContent(120)) {
									throw new IllegalStateException(section + " content did not resume after the mid-roll check");
								}
								boolean midRollRequested = ads.isAdRequested();
								if (Boolean.getBoolean("ads.seek.debug")) {
									ads.saveSeekDebug();
									ads.clearAdRequests();
									ads.startManualSeekDebug();
									for (int second = 0; second < 180; second++) {
										networkUtils.getCapturedRequests();
										Thread.sleep(1000);
									}
									ads.saveSeekDebug();
									logger.info("Manual comparison ad request found: {}. Automated result remains unchanged.", ads.isAdRequested());
								}
								logger.info("{} - {} mid-roll: ad visible = {}, ad request found = {}",
										plan, section, midRollVisible, midRollRequested);
								test.info(plan + " - " + section + " mid-roll: ad visible = " + midRollVisible
										+ ", ad request found = " + midRollRequested);
								boolean midRollSeen = midRollVisible || midRollRequested;

								String midRollResult = plan + " - " + section + " mid-roll: ad seen = " + midRollSeen + ", ad expected = " + expectsAds;
								logger.info(midRollResult);
								if (midRollSeen == expectsAds) {
									test.pass(midRollResult);
								} else {
									test.fail(midRollResult);
									failures.add(midRollResult);
								}
							} else {
								String notChecked = plan + " - " + section + " mid-roll NOT checked: no title longer than " + midRollTime + " found";
								test.warning(notChecked);
								failures.add(notChecked);
							}

						} catch (Exception e) {
							String message = plan + " - " + section + " stopped with error: " + e.getMessage();
							logger.error(message);
							test.fail(message);
							failures.add(message);
						}
					}
				}

				// ==================== SUN SHORTS ====================

				//                try {
				//                    driver.get(homeUrl);
				//                    HomePageSunShorts shortsHome = new HomePageSunShorts(driver);
				//                    shortsHome.shortsNavigation();
				//
				//                    ads.clearAdRequests();
				//                    String shortsTitle = shortsHome.contentPlayBackCheck(-1, -1);
				//                    test.info(plan + " - Shorts title: " + shortsTitle);
				//
				//                    PlayerSunShorts player = new PlayerSunShorts(driver);
				//                    player.waitForPlaybackProgress(60);
				//                    player.fullScreen();
				//
				//                    // If the random title opened on a later episode, go back to episode 1
				//                    for (int back = 0; back < 20 && player.getActiveEpisodeNumber() > 1; back++) {
				//                        player.previousEpisode();
				//                        Thread.sleep(3000);
				//                    }
				//
				//                    // ---------- LOOP 4: episodes 1 to 6. The ad should come only on the 6th (and only for ad plans) ----------
				//                    for (int episode = 1; episode <= shortsAdEpisode; episode++) {
				//                        boolean adSeen;
				//                        if (episode == shortsAdEpisode) {
				//                            // 6th: ad visible, else ad request
				//                            adSeen = ads.waitForAd(longWait);
				//                            if (!adSeen) {
				//                                adSeen = ads.isAdRequested();
				//                                test.info(plan + " - Shorts episode " + episode + " ad not visible, ad request found = " + adSeen);
				//                            }
				//                        } else {
				//                            // 1 to 5: only a visible ad counts
				//                            adSeen = ads.waitForAd(shortWait);
				//                        }
				//
				//                        boolean adExpected = expectsAds && episode == shortsAdEpisode;
				//                        String shortsResult = plan + " - Shorts episode " + episode + ": ad seen = " + adSeen + ", ad expected = " + adExpected;
				//                        logger.info(shortsResult);
				//                        if (adSeen == adExpected) {
				//                            test.pass(shortsResult);
				//                        } else {
				//                            test.fail(shortsResult);
				//                            failures.add(shortsResult);
				//                        }
				//
				//                        if (episode < shortsAdEpisode) {
				//                            ads.clearAdRequests();              // forget old requests, then go to next episode
				//                            player.nextEpisode();
				//
				//                            // wait until the next episode is highlighted
				//                            boolean episodeChanged = false;
				//                            for (int second = 1; second <= 20; second++) {
				//                                try {
				//                                    if (player.getActiveEpisodeNumber() == episode + 1) {
				//                                        episodeChanged = true;
				//                                        break;
				//                                    }
				//                                } catch (RuntimeException e) {
				//                                    logger.info("Highlighted episode not visible yet");
				//                                }
				//                                Thread.sleep(1000);
				//                            }
				//                            if (!episodeChanged) {
				//                                throw new RuntimeException("Episode " + (episode + 1) + " did not become active");
				//                            }
				//                        }
				//                    }
				//
				//                } catch (Exception e) {
				//                    String message = plan + " - Sun Shorts stopped with error: " + e.getMessage();
				//                    logger.error(message);
				//                    test.fail(message);
				//                    failures.add(message);
				//                }
				//
				//            } catch (Exception e) {
				// String message = plan + " - flow stopped with error: " + e.getMessage();
				//                logger.error(message);
				//                test.fail(message);
				//                failures.add(message);
			} finally {
				if (Boolean.getBoolean("ads.seek.debug") && driver != null) {
					try {
						new AdsCheckPage(driver).savePlayerDebug();
					} catch (Exception e) {
						logger.warn("Could not save player debug state: {}", e.getClass().getSimpleName());
					}
				}
				if (networkUtils != null) {
					networkUtils.getCapturedRequests();
				}
				quitDriver();
			}
		}

		Assert.assertTrue(failures.isEmpty(), "Ad checks failed:\n" + String.join("\n", failures));
	}
}
