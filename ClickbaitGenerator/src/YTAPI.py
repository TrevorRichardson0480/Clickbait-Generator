# Code generated by YouTube API v3, we have made modifications to the generated code
# https://developers.google.com/explorer-help/guides/code_samples#python

import os
import google_auth_oauthlib.flow
import googleapiclient.discovery
import googleapiclient.errors
import sys

scopes = ["https://www.googleapis.com/auth/youtube.readonly"]


def main():
    countryCode = str(sys.argv[1])
    videoID = str(sys.argv[2])

    if countryCode == "None":
        countryCode = "US"

    if videoID == "None":
        videoID = ""

    os.environ["OAUTHLIB_INSECURE_TRANSPORT"] = "0"

    # set service, version, and authorization credentials
    api_service_name = "youtube"
    api_version = "v3"
    developerKey = "INSERT KEY HERE"

    youtube = googleapiclient.discovery.build(api_service_name, api_version, developerKey=developerKey)

    # current page is blank, add text "nextPageToken" to iterate while loop, init page ID and file number
    response = "nextPageToken"
    nextPageID = ""
    fileNum = 0

    # while pages remain, get next 10 results, output to new file, get next page ID
    while "nextPageToken" in response:
        request = youtube.videos().list(
            part="snippet,contentDetails,statistics",
            chart="mostPopular",
            maxResults=10,
            pageToken=nextPageID,
            regionCode=countryCode,
            videoCategoryId=videoID
        )

        # get response: execute request, get string, encode and decode for file writing
        response = str(request.execute()).encode('utf-8').decode('ascii', 'ignore')
        # open file, write response to file
        file = open("../outputFiles/outputFile" + str(fileNum) + ".txt", "w")
        file.write(response)
        # get next page ID from response, increment file num
        nextPageID = response[response.find("nextPageToken") + 17: response.find("nextPageToken") + 17 + response[response.find("nextPageToken") + 17:].find("\'")]
        fileNum += 1


if __name__ == "__main__":
    main()