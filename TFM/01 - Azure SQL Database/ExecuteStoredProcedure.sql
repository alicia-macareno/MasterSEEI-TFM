DECLARE @responseMessage NVARCHAR(250)

EXEC dbo.uspAddChildAccount
          @pLoginEmail = 'alicia.macareno@outlook.com',
          @pParentAccountEmail = 'aaperezmac@hotmail.com',
          @pPassword = '123abc',
          @pFirstName = 'Alicia',
          @pFirstLastName = 'P�rez',
		  @pSecondLastName = 'Macareno'

SELECT *
FROM [dbo].[ChildAccount]