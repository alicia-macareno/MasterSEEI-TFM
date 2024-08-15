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
    using Microsoft.AspNetCore.Mvc;
    using Newtonsoft.Json;

    public static class GetChildAccountInfo
    {
        [FunctionName("GetChildAccountInfo")]
        public static async Task<HttpResponseMessage> Run([HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "ChildAccountInfo")] HttpRequestMessage req, Microsoft.Extensions.Logging.ILogger logger)
        {
            logger.LogInformation("GetChildAccountInfo Azure API execution started...");
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
                    var command = new SqlCommand("dbo.uspGetChildAccountInfo", conn);
                    command.CommandType = CommandType.StoredProcedure;
                    //Add procedure parameters
                    command.Parameters.Add(new SqlParameter("@pLoginEmail", email));
                    command.Parameters.Add(new SqlParameter("@pPassword", password));

                    command.Parameters.Add(new SqlParameter("@responseMessage", SqlDbType.NVarChar, 250));
                    command.Parameters["@responseMessage"].Direction = ParameterDirection.Output;

                    command.Parameters.Add(new SqlParameter("@responseCode", SqlDbType.Int));
                    command.Parameters["@responseCode"].Direction = ParameterDirection.Output;

                    command.Parameters.Add(new SqlParameter("@FirstName", SqlDbType.NVarChar, 40));
                    command.Parameters["@FirstName"].Direction = ParameterDirection.Output;

                    command.Parameters.Add(new SqlParameter("@FirstLastName", SqlDbType.NVarChar, 40));
                    command.Parameters["@FirstLastName"].Direction = ParameterDirection.Output;

                    command.Parameters.Add(new SqlParameter("@Perimeter", SqlDbType.Int));
                    command.Parameters["@Perimeter"].Direction = ParameterDirection.Output;

                    command.Parameters.Add(new SqlParameter("@RealTimeMonitoring", SqlDbType.Bit));
                    command.Parameters["@RealTimeMonitoring"].Direction = ParameterDirection.Output;

                    command.Parameters.Add(new SqlParameter("@BaseLocationLatitude", SqlDbType.Float));
                    command.Parameters["@BaseLocationLatitude"].Direction = ParameterDirection.Output;

                    command.Parameters.Add(new SqlParameter("@BaseLocationLongitude", SqlDbType.Float));
                    command.Parameters["@BaseLocationLongitude"].Direction = ParameterDirection.Output;

                    command.Parameters.Add(new SqlParameter("@ContactPhone", SqlDbType.NVarChar, 20));
                    command.Parameters["@ContactPhone"].Direction = ParameterDirection.Output;

                    try
                    {
                        logger.LogInformation("Let's execute procedure!");
                        command.ExecuteNonQuery();
                        logger.LogInformation("Executed! :)");

                        ChildAccountInfo childAccountInfo = new ChildAccountInfo();

                        childAccountInfo.FirstName = Convert.ToString(command.Parameters["@FirstName"].Value);
                        logger.LogInformation(childAccountInfo.FirstName.ToString());
                        childAccountInfo.FirstLastName = Convert.ToString(command.Parameters["@FirstLastName"].Value);
                        logger.LogInformation(childAccountInfo.FirstLastName.ToString());

                        if (!(command.Parameters["@RealTimeMonitoring"].Value is DBNull))
                        {
                            childAccountInfo.RealTimeMonitoring = Convert.ToBoolean(command.Parameters["@RealTimeMonitoring"].Value);
                        }
                        
                        logger.LogInformation(Convert.ToString(childAccountInfo.RealTimeMonitoring.ToString()));
                        if (!(command.Parameters["@Perimeter"].Value is DBNull))
                        {
                            childAccountInfo.Perimeter = Convert.ToInt16(command.Parameters["@Perimeter"].Value);
                        }
                        
                        if(!(command.Parameters["@BaseLocationLongitude"].Value is DBNull))
                        {
                            childAccountInfo.BaseLocationLongitude = Convert.ToDouble(command.Parameters["@BaseLocationLongitude"].Value);
                        }

                        if(!(command.Parameters["@BaseLocationLatitude"].Value is DBNull))
                        {
                            childAccountInfo.BaseLocationLatitude = Convert.ToDouble(command.Parameters["@BaseLocationLatitude"].Value);
                        }

                        if (!(command.Parameters["@ContactPhone"].Value is DBNull))
                        {
                            childAccountInfo.ContactPhone = Convert.ToString(command.Parameters["@ContactPhone"].Value);
                        }

                        logger.LogInformation($"FirstName: {childAccountInfo.FirstName}, FirstLastName: {childAccountInfo.FirstLastName}, Perimeter: {childAccountInfo.Perimeter}, RealTimeMonitoring: {childAccountInfo.RealTimeMonitoring}  ");
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
                        response.Content = new StringContent(JsonConvert.SerializeObject(childAccountInfo));
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