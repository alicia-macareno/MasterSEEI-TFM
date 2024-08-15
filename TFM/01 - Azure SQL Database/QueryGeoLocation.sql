SELECT
ChildAccount,
CreatedOn,
GeoLocation,
GeoLocation.Lat AS Latitude,
Geolocation.Long AS Longitude
FROM [dbo].[UserLocation]