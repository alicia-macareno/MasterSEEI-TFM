DECLARE	@responseMessage nvarchar(250)

--Correct login and password
EXEC	dbo.uspParentUserLogin
		@pLoginEmail = N'',
		@pPassword = N'',
		@responseMessage = @responseMessage OUTPUT

SELECT	@responseMessage as N'@responseMessage'