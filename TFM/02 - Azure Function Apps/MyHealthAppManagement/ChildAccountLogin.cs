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

    public static class ChildAccountLogin
    {
        [FunctionName("ChildAccountLogin")]
        public static async Task<HttpResponseMessage> Run([HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "ChildAccountLogin")] HttpRequestMessage req, Microsoft.Extensions.Logging.ILogger logger)
        {
            logger.LogInformation("ChildAccountLogin Azure API execution started...");
            HttpResponseMessage response = new HttpResponseMessage();
            // Get request body
            dynamic data = await req.Content.ReadAsAsync<object>();
            //Validate entry
            if (data.email == string.Empty || data.email == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Email cannot be empty nor null"); return response; }
            if (data.password == string.Empty || data.password == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Password cannot be empty nor null"); return response; }

            var str = Environment.GetEnvironmentVariable("AZURE_SQL_CONNECTIONSTRING")!;
            string email = data.email;
            string password = data.password;

            using (SqlConnection conn = new SqlConnection(str))
            {
                try
                {
                    conn.Open();
                    logger.LogInformation("Successful connection to DB!");
                    //Set stored procedure
                    var command = new SqlCommand("dbo.uspChildAccountLogin", conn);
                    command.CommandType = CommandType.StoredProcedure;
                    //Add procedure parameters
                    command.Parameters.Add(new SqlParameter("@pLoginEmail", email));
                    command.Parameters.Add(new SqlParameter("@pPassword", password));
                    command.Parameters.Add(new SqlParameter("@responseMessage", SqlDbType.NVarChar, 250));
                    command.Parameters["@responseMessage"].Direction = ParameterDirection.Output;
                    command.Parameters.Add(new SqlParameter("@responseCode", SqlDbType.Int));
                    command.Parameters["@responseCode"].Direction = ParameterDirection.Output;

                    try
                    {
                        logger.LogInformation("Let's execute procedure!");
                        command.ExecuteNonQuery();
                        logger.LogInformation("Executed! :)");
                        switch (command.Parameters["@responseCode"].Value)
                        {
                            case 200:
                                response.StatusCode = HttpStatusCode.OK;
                                break;

                            case 400:
                                response.StatusCode = HttpStatusCode.BadRequest;
                                break;

                            default:
                                response.StatusCode = HttpStatusCode.NotFound;
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