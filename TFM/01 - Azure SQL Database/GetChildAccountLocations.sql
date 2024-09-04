DECLARE @responseMessage NVARCHAR(250);
DECLARE @responseCode INT;

EXEC [dbo].[uspGetLastUserLocations]
    @pChildAccountEmail = '',
    @pParentAccountEmail = '',
    @pParentAccountPassword = '',
    @responseMessage = @responseMessage OUTPUT,
    @responseCode = @responseCode OUTPUT;

-- Imprimir los valores de salida
SELECT @responseMessage AS ResponseMessage, @responseCode AS ResponseCode;
