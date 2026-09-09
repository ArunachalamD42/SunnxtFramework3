# Entitlement Test Data Structure

## File: `entitlement-testdata.xlsx`

### Sheet: "PlanConfig"

| Column | Field | Description |
|--------|-------|-------------|
| A | planName | Plan display name |
| B | username | Test account mobile/email |
| C | password | Test account password |
| D | contentScope | SN+SNS / SN+FreeSNS / SNS+FreeSN |
| E | adBehavior | ad-free / with-ads |
| F | snsAdTier | free / lite / premium |
| G | maxVideoQuality | 4K UHD / Full HD |
| H | audioQuality | Dolby Atmos/DD+5.1 / DD+5.1 / Stereo 2.0 |
| I | liveTvAccess | yes / no |
| J | loginLimit | Number (1-4) |
| K | deviceLimit | Number (1-4) |
| L | contentLockThreshold | 30 / 200 |
| M | snContentUrl | URL path for SN content to test |
| N | snsContentUrl | URL path for SNS Shorts content to test |

### Rows (7 plans):

| planName | username | password | contentScope | adBehavior | snsAdTier | maxVideoQuality | audioQuality | liveTvAccess | loginLimit | deviceLimit | contentLockThreshold | snContentUrl | snsContentUrl |
|----------|----------|----------|--------------|-----------|-----------|-----------------|--------------|-------------|------------|-------------|---------------------|--------------|---------------|
| Free User | (your free user) | (password) | SN+SNS | with-ads | free | Full HD | Stereo 2.0 | no | 1 | 1 | 30 | /movies/... | /shorts/... |
| Combo Annual Platinum | (user) | (pass) | SN+SNS | ad-free | premium | 4K UHD | Dolby Atmos/DD+5.1 | yes | 4 | 4 | 200 | /movies/... | /shorts/... |
| Combo Quarterly Platinum | (user) | (pass) | SN+SNS | with-ads | lite | Full HD | Stereo 2.0 | yes | 2 | 1 | 200 | /movies/... | /shorts/... |
| Sun NXT Annual Premium | (user) | (pass) | SN+FreeSNS | ad-free | premium | Full HD | DD+5.1 | yes | 3 | 2 | 200 | /movies/... | /shorts/... |
| Sun NXT Quarterly Lite | (user) | (pass) | SN+FreeSNS | with-ads | lite | Full HD | Stereo 2.0 | yes | 2 | 1 | 200 | /movies/... | /shorts/... |
| Sun NXT Shorts Annual Premium | (user) | (pass) | SNS+FreeSN | ad-free | premium | Full HD | DD+5.1 | no | 3 | 2 | 200 | /movies/... | /shorts/... |
| Sun NXT Shorts Quarterly Lite | (user) | (pass) | SNS+FreeSN | with-ads | lite | Full HD | Stereo 2.0 | no | 2 | 1 | 200 | /movies/... | /shorts/... |

### Sheet: "TestContent"

| Column | Field | Description |
|--------|-------|-------------|
| A | contentType | SN_Content / SNS_Content |
| B | contentUrl | URL path (e.g., /movies/some-slug) |
| C | contentTitle | Display name for logging |
| D | hasMultiAudio | yes / no |
| E | has4KVersion | yes / no |
