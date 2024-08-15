DECLARE @responseMessage NVARCHAR(250)

EXEC dbo.uspAddUserLocation
          @pChildAccountEmail = 'alicia.macareno@outlook.com',
		  @pLatitude = 36.7152,
		  @pLongitude = -4.463
SELECT
ChildAccount,
CreatedOn,
GeoLocation.Lat AS Latitude,
Geolocation.Long AS Longitude
FROM [dbo].[UserLocation]