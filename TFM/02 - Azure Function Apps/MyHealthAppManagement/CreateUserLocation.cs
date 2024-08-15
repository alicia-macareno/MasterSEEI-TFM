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

    public static class CreateUserLocation
    {
        [FunctionName("CreateUserLocation")]
        public static async Task<HttpResponseMessage> Run([HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "UserLocation")] HttpRequestMessage req, Microsoft.Extensions.Logging.ILogger logger)
        {
            logger.LogInformation("CreateUserLocation Azure API execution started...");
            HttpResponseMessage response = new HttpResponseMessage();
            // Get request body
            dynamic data = await req.Content.ReadAsAsync<object>();
            //Validate entry
            if (data.ChildAccountEmail == string.Empty || data.ChildAccountEmail == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Child account emailcannot be empty nor null"); return response; }
            if (data.Latitude == float.NaN || data.Latitude == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Latitude cannot must be a number and not null"); return response; }
            if (data.Longitude == float.NaN || data.Longitude == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Longitude cannot must be a number and not null"); return response; }

            NewUserLocation userLocation = new NewUserLocation();

            //Get account info from request
            userLocation.Latitude = data?.Latitude;
            userLocation.Longitude = data?.Longitude;
            userLocation.ChildAccountEmail = data?.ChildAccountEmail;

            var str = Environment.GetEnvironmentVariable("AZURE_SQL_CONNECTIONSTRING")!;

            using (SqlConnection conn = new SqlConnection(str))
            {
                try
                {
                    conn.Open();
                    logger.LogInformation("Successful connection to DB!");
                    //Set stored procedure
                    var command = new SqlCommand("dbo.uspAddUserLocation", conn);
                    command.CommandType = CommandType.StoredProcedure;
                    //Add procedure parameters
                    command.Parameters.Add(new SqlParameter("@pChildAccountEmail", userLocation.ChildAccountEmail));
                    command.Parameters.Add(new SqlParameter("@pLatitude", userLocation.Latitude));
                    command.Parameters.Add(new SqlParameter("@pLongitude", userLocation.Longitude));
                    command.Parameters.Add(new SqlParameter("@responseMessage", SqlDbType.NVarChar, 250));
                    command.Parameters["@responseMessage"].Direction = ParameterDirection.Output;
                    command.Parameters.Add(new SqlParameter("@responseCode", SqlDbType.Int));
                    command.Parameters["@responseCode"].Direction = ParameterDirection.Output;

                    try
                    {
                        logger.LogInformation("Let's execute procedure!");
                        command.ExecuteNonQuery();
                        switch (command.Parameters["@responseCode"].Value)
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