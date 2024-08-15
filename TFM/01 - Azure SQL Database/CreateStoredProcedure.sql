
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[uspChildAccountLogin]
(
    @pLoginEmail NVARCHAR(254),
    @pPassword NVARCHAR(50),
    @responseMessage NVARCHAR(250)='' OUTPUT,
	@responseCode INT=404 OUTPUT
)
AS
BEGIN
 SET NOCOUNT ON

    DECLARE @ChildAccountID INT

    IF EXISTS (SELECT TOP 1 ChildAccountID FROM [dbo].[ChildAccount] WHERE LoginEmail=@pLoginEmail)
    BEGIN
        SET @ChildAccountID=(SELECT ChildAccountID FROM [dbo].[ChildAccount] WHERE LoginEmail=@pLoginEmail AND PasswordHash=HASHBYTES('SHA2_512', @pPassword+CAST(Salt AS NVARCHAR(36))))

       IF(@ChildAccountID IS NULL) 
		BEGIN
			UPDATE dbo.ChildAccount SET LastFailedLogin = GETUTCDATE() WHERE LoginEmail=@pLoginEmail
           SET @responseMessage='Incorrect email or password'
		   SET @responseCode = 400
	    END
       ELSE 
	    BEGIN
		   UPDATE dbo.ChildAccount SET LastSuccessfulLogin = GETUTCDATE() WHERE LoginEmail=@pLoginEmail
           SET @responseMessage='User successfully logged in'
		   SET @responseCode = 200
	    END
    END
    ELSE
       SET @responseMessage='User not found'
END
GO

GRANT EXECUTE ON OBJECT :: [dbo].[uspChildAccountLogin] TO myhealthappmanagement