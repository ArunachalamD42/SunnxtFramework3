# Ads playback: one complete flow per account

Run `testng-ads.xml` as a TestNG Suite in Eclipse. Each enabled/configured account logs in once and runs `completeAdsPlaybackTest` in this order:

1. Movies: select a random title, verify playback and pre-roll, seek to 15:00, then check mid-roll.
2. Live TV: select a random channel, verify playback and pre-roll only.
3. TV Shows: select a random title, verify playback and pre-roll, seek to 15:00, then check mid-roll.
4. Shorts: select random content and start at episode 1. SN Annual Premium and SN Quarterly Lite must play episodes 1-7, then selecting episode 8 must show Keep the Drama Going while episode 7 remains the active playing episode. Other plans keep the six-episode sample. Watch each permitted episode for 10 seconds; the ad trigger remains entry to episode 6.

All steps and assertions appear directly in the test method. Only report setup/cleanup and individual page actions remain separate. Credential checks and browser launch are inside the test, so a missing account is a test skip rather than a configuration skip when running the class directly. The regression suite and legacy entitlement tests are unchanged. Concurrency is excluded.

## Expected outcomes

| Account plan | Movie / Live TV / TV Show | Shorts |
| --- | --- | --- |
| Combo Annual Platinum | Playback; no ads during observation | Playback; no ads during observation |
| SN Annual Premium | Playback; no ads during observation | Episodes 1-7 play without detected ads; episode 8 shows subscription popup |
| Combo Quarterly Platinum | Playback with pre-roll and VOD mid-roll checks | Sixth-episode ad check |
| SN Quarterly Lite | Playback with pre-roll and VOD mid-roll checks | Episodes 1-7 play, sixth-episode ad check; episode 8 shows subscription popup |
| SNS Annual Premium | Attempt playback and verify subscription restriction | Playback; no ads during observation |
| SNS Quarterly Lite | Attempt playback and verify subscription restriction | Sixth-episode ad check |

A restriction requires the existing Subscribe to Watch control or subscription prompt AND no advancing content playback. A missing Play button or timeout alone never passes a restriction check. These checks use the randomly selected titles/channels. The Free marker is now supplied as img[alt=free_badge], but selecting only titles without that marker still needs the complete card HTML to establish its relationship to the poster; a randomly selected free title may legitimately play, so investigate that status before treating an SNS-only restriction failure as a product defect.

For SN-only plans, the random Shorts title must include episode 8; otherwise the paid boundary check is explicitly skipped. The popup is checked by its supplied h2 text; absence of playback alone does not prove the restriction. The final result passes only when every required section is verified. A title shorter than 15:00 or unavailable network evidence records an unverified ad check, continues to the next section, and leaves the final result skipped. Assertion failures still stop the test normally; later sections are not claimed as tested.

## User configuration file

Edit `testdata/ads-users.local.properties` in Eclipse. This local file is ignored by Git and stays outside packaged application resources. `testdata/ads-users.example.properties` is the credential-free reference/default file.

For each user:

- Set `ads.<ALIAS>.enabled=true` to run it, or `false` to leave it out.
- `ads.<ALIAS>.plan` is its report label.
- `ads.<ALIAS>.sn.expected=PLAY` or `BLOCK` controls Movie, Live TV and TV Show expectations.
- `ads.<ALIAS>.ads.expected=ADS` or `NO_ADS` controls ad expectations.
- `ads.<ALIAS>.shorts.expected=FULL` runs the six-episode sample. `FIRST_7` verifies episodes 1-7 play and episode 8 is restricted.
- Fill that user's blank `SUNNXT_ENTITLEMENT_<ALIAS>_CREDENTIAL_MOBILE` and `..._PASSWORD` slots locally, or continue supplying the complete pair through Eclipse environment variables.

Quarterly Platinum already references your existing ConfigReader keys using:

```properties
ads.COMBO_QUARTERLY.username.config.key=test.platinumCombo
ads.COMBO_QUARTERLY.password.config.key=test.password1
```

These are key names, not credential values. Other users can reference their own existing key pair the same way. This removes the hardcoded Quarterly credential mapping from the test.

Set `SUNNXT_AD_OBSERVATION_SECONDS` to your chosen positive duration in the local file, environment, or existing ConfigReader properties. No default observation duration is invented. Shorts still waits 10 seconds per permitted episode.

The enabled Annual Combo / Quarterly Combo selection is carried over from the existing DataProvider. If only Quarterly credentials are available, set `ads.COMBO_ANNUAL.enabled=false` locally. You can now change the enabled users and expected behaviour without editing Java.

Credential priority is: complete runtime environment pair, account-specific ConfigReader values (including the local user file), then the configured existing-key reference pair. A partial environment pair is reported explicitly and is never silently combined with a configuration password. Missing credentials skip that account inside the test, so direct class Run All can continue with other users.

ConfigReader loads the example, then the local overrides. JVM properties retain priority over file settings; nonblank environment values take priority through getRuntime. Blank credential placeholders do not hide values supplied through existing ConfigReader properties. The local file starts with blank credential slots; no existing secret values are copied into it. Never put credentials in the example file, Java, XML, Git, or shared Eclipse launch files.

Refresh the project with F5 after editing. Run either the class or `testng-ads.xml`. Each user must actually have the stated subscription; the test does not purchase or verify the account's plan name.

## Ad/network checks

Ad visibility and request detection are recorded separately. For ad-supported checks, either a visible playing Pulse ad or a captured ad request is sufficient. Content playback must pass independently; the ad video is excluded from that check. Annual-plan checks fail on detected ads, and missing capture cannot prove no ads.

NetworkUtils reads ChromeDriver performance logs using the raw Network.enable command, without a version-specific DevTools import. It recognises the existing suntv.videoplaza host family and exact source URLs read from Pulse ad-video elements. Ordinary videos on the same CDN are not automatically classified as ads. New ad services outside these recognised sources need verified request evidence before classification can be extended. No-ad results apply only to the observed sample and recognised sources.

Each observation drains older buffered requests. Read failures or no captured traffic prevent an ad-free pass. Raw captured URLs are not printed. The unchanged LoginPage still logs the login ID; do not commit runtime logs/reports.

## Verification

Browser-independent checks:

`mvn -o -Dtest=AdsUsersConfigTest,ConfigReaderRuntimeTest,AdsPlaybackDataTest test`

Opt-in local headless Chrome capture check (no subscription account):

`mvn -o -Dtest=NetworkCaptureSmokeTest -DrunNetworkSmoke=true -Dnetwork.smoke.driver=<local-chromedriver-executable> test`

The local capture smoke check is separate from the complete live subscription flow. Chrome may still print a typed DevTools binding warning while performance-log capture works independently.
