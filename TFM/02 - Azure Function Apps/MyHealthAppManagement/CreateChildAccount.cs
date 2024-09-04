namespace myhealthcaresystem
{
    using Microsoft.Azure.WebJobs;
    using Microsoft.Azure.WebJobs.Extensions.Http;
    using Microsoft.Extensions.Logging;
    using MyHealthAppManagement.Common;
    using System;
    using Microsoft.Data.SqlClient;
    using System.Net;
    using System.Net.Http;
    using System.Threading.Tasks;
    using System.Data;

    public static class CreateChildAccount
    {
        [FunctionName("CreateChildAccount")]
        public static async Task<HttpResponseMessage> Run([HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "ChildAccounts")] HttpRequestMessage req, ILogger logger)
        {
            logger.LogInformation("CreateChild Azure API execution started...");
            HttpResponseMessage response = new HttpResponseMessage();

            // Get request body
            dynamic data = await req.Content.ReadAsAsync<object>();

            // Validate entry
            if (data.email == string.Empty || data.email == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Email cannot be empty nor null"); return response; }
            if (data.password == string.Empty || data.password == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Password cannot be empty nor null"); return response; }
            if (data.firstName == string.Empty || data.firstName == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("First name cannot be empty nor null"); return response; }
            if (data.firstLastName == string.Empty || data.firstLastName == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("First last name cannot be empty nor null"); return response; }
            if (data.parentAccountEmail == string.Empty || data.parentAccountEmail == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Parent account email cannot be empty nor null"); return response; }
            if (data.latitude == null || data.longitude == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Latitude and Longitude cannot be null"); return response; }
            if (data.perimeter == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Perimeter cannot be null"); return response; }

            // Create account data object
            NewChildAccount accountData = new NewChildAccount
            {
                Email = data.email,
                Password = data.password,
                firstName = data.firstName,
                firstLastName = data.firstLastName,
                secondLastName = data.secondLastName,
                parentAccountEmail = data.parentAccountEmail,
                latitude = data.latitude,
                longitude = data.longitude,
                perimeter = data.perimeter,
                realTimeMonitoring = data.realTimeMonitoring

            };

            var str = Environment.GetEnvironmentVariable("AZURE_SQL_CONNECTIONSTRING")!;

            using (SqlConnection conn = new SqlConnection(str))
            {
                try
                {
                    conn.Open();
                    logger.LogInformation("Successful connection to DB!");

                    // Set stored procedure
                    var command = new SqlCommand("dbo.uspAddChildAccount", conn)
                    {
                        CommandType = CommandType.StoredProcedure
                    };

                    // Add procedure parameters
                    command.Parameters.Add(new SqlParameter("@pLoginEmail", accountData.Email));
                    command.Parameters.Add(new SqlParameter("@pPassword", accountData.Password));
                    command.Parameters.Add(new SqlParameter("@pFirstName", accountData.firstName));
                    command.Parameters.Add(new SqlParameter("@pFirstLastName", accountData.firstLastName));
                    command.Parameters.Add(new SqlParameter("@pSecondLastName", accountData.secondLastName));
                    command.Parameters.Add(new SqlParameter("@pParentAccountEmail", accountData.parentAccountEmail));
                    command.Parameters.Add(new SqlParameter("@pLatitude", SqlDbType.Float) { Value = accountData.latitude });
                    command.Parameters.Add(new SqlParameter("@pLongitude", SqlDbType.Float) { Value = accountData.longitude });
                    command.Parameters.Add(new SqlParameter("@pPerimeter", SqlDbType.Int) { Value = accountData.perimeter });
                    command.Parameters.Add(new SqlParameter("@responseMessage", SqlDbType.NVarChar, 250));
                    command.Parameters.Add(new SqlParameter("@pRealTimeMonitoring", SqlDbType.Bit) { Value = accountData.realTimeMonitoring ? 1 : 0 });
                    command.Parameters["@responseMessage"].Direction = ParameterDirection.Output;
                    command.Parameters.Add(new SqlParameter("@responseCode", SqlDbType.Int));
                    command.Parameters["@responseCode"].Direction = ParameterDirection.Output;

                    try
                    {
                        logger.LogInformation("Let's execute procedure!");
                        command.ExecuteNonQuery();
                        switch ((int)command.Parameters["@responseCode"].Value)
                        {
                            case 201:
                                response.StatusCode = HttpStatusCode.Created;
                                break;
                            default:
                                response.StatusCode = HttpStatusCode.Conflict;
                                break;
                        }
                        response.ReasonPhrase = Convert.ToString(command.Parameters["@responseMessage"].Value);
                    }
                    catch (Exception ex)
                    {
                        response.StatusCode = HttpStatusCode.InternalServerError;
                        logger.LogInformation(ex.Message);
                    }
                }
                catch (Exception ex)
                {
                    response.StatusCode = HttpStatusCode.InternalServerError;
                    logger.LogInformation(ex.Message);
                }
            }
            return response;
        }
    }

   
}
