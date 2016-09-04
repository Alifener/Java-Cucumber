Feature: beamtv

  Free text area
  Contains list of scenarios
  User's title should be displayed on the web page
  User should be able to see and check the project name on the web page
  User should be able to show/hide metadata
  User should be able to see the details of the file when necessary
  User should be able to download multiple files
  User should be able to playback/view a group of files when the link goes offline


  Scenario Outline:Display Title and Check Project Name

    Given As a <user>
    #User is defined as a variable to be able to cover another users with same scenario/causes.
    #The encoded user name captured from the webpage url
    When I go to the project web page
    Then I should have my <title> displayed in the project so that I know which <project> I am working on

    # I noticed the last part of the url was b64 encoded and it was matching the email address on the bottom of the page
    Examples:
      | user          | title           | project |
      | adamd@beam.tv | FileMailer Test | J107066 |

  Scenario Outline: Show / Hide Metadata and Displaying File Details

    Given As a <user>
    When I go to the project web page
    Then I should have an option to show/hide metadata
    And So that I should be able to see details of the file when necessary

    Examples:
      | user          |
      | adamd@beam.tv |

  Scenario Outline: Click Download Button and Download Multiple Files and Playing The Files

    Given As a <user>
    When I go to the project web page
    And I click the download button
    Then I should be able to download multiple files
    And So that I should be able to playback/view a group of files when the link goes offline

    Examples:
      | user          |
      | adamd@beam.tv |
