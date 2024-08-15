namespace myhealthcaresystem
{
    using Microsoft.Azure.WebJobs;
    using Microsoft.Azure.WebJobs.Extensions.Http;
    using Microsoft.Extensions.Logging;
    using System;
    using Microsoft.Data.SqlClient;
    using System.Net;
    using System.Net.Http;
    using System.Threading.Tasks;
    using System.Data;
    using Microsoft.AspNetCore.Mvc;
    using Newtonsoft.Json;
    using System.Collections.Generic;
    using MyHealthAppManagement.Common;

    public static class GetChildAccounts
    {
        [FunctionName("GetAssociatedChildAccounts")]
        public static async Task<HttpResponseMessage> Run(
            [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "GetAssociatedChildAccounts")] HttpRequestMessage req,
            ILogger logger)
        {
            logger.LogInformation("GetChildAccounts Azure API execution started...");

            HttpResponseMessage response = new HttpResponseMessage();

            // Get request body
            dynamic data = await req.Content.ReadAsAsync<object>();

            // Validate entry
            if (data.email == string.Empty || data.email == null)
            {
                response.StatusCode = HttpStatusCode.BadRequest;
                response.Content = new StringContent("Email cannot be empty nor null");
                return response;
            }

            var str = Environment.GetEnvironmentVariable("AZURE_SQL_CONNECTIONSTRING");
            string email = data.email;

            List<ChildAccount> childAccounts = new List<ChildAccount>();

            using (SqlConnection conn = new SqlConnection(str))
            {
                try
                {
                    conn.Open();
                    logger.LogInformation("Successful connection to DB!");

                    // Set stored procedure
                    var command = new SqlCommand("dbo.uspGetChildAccounts", conn);
                    command.CommandType = CommandType.StoredProcedure;

                    // Add procedure parameters
                    command.Parameters.Add(new SqlParameter("@pParentAccountEmail", email));

                    try
                    {
                        logger.LogInformation("Executing procedure...");
                        using (SqlDataReader reader = await command.ExecuteReaderAsync())
                        {
                            while (reader.Read())
                            {
                                ChildAccount childAccount = new ChildAccount
                                {
                                    ChildAccountID = reader.GetInt32(reader.GetOrdinal("ChildAccountID")),
                                    LoginEmail = reader.GetString(reader.GetOrdinal("LoginEmail")),
                                    FirstName = reader.GetString(reader.GetOrdinal("FirstName")),
                                    FirstLastName = reader.GetString(reader.GetOrdinal("FirstLastName")),
                                    SecondLastName = reader.GetString(reader.GetOrdinal("SecondLastName")),
                                    Status = reader.GetBoolean(reader.GetOrdinal("Status")),
                                    Blocked = reader.GetBoolean(reader.GetOrdinal("Blocked")),
                                    FailedLoginAttempts = reader.GetInt32(reader.GetOrdinal("FailedLoginAttempts")),
                                    CreatedOn = reader.GetDateTime(reader.GetOrdinal("CreatedOn")),
                                    RealTimeMonitoring = reader.GetBoolean(reader.GetOrdinal("RealTimeMonitoring")),
                                    Perimeter = reader.GetInt32(reader.GetOrdinal("Perimeter")),
                                    PendingLocationConfig = reader.GetBoolean(reader.GetOrdinal("PendingLocationConfig"))
                                };

                                childAccounts.Add(childAccount);
                            }
                        }

                        logger.LogInformation("Procedure executed successfully!");

                        response.StatusCode = HttpStatusCode.OK;
                        response.Content = new StringContent(JsonConvert.SerializeObject(childAccounts));
                    }
                    catch (Exception ex)
                    {
                        response.StatusCode = HttpStatusCode.InternalServerError;
                        response.Content = new StringContent($"Error executing procedure: {ex.Message}");
                        logger.LogError(ex, "Error executing stored procedure.");
                    }
                }
                catch (Exception ex)
                {
                    response.StatusCode = HttpStatusCode.InternalServerError;
                    response.Content = new StringContent($"Error connecting to the database: {ex.Message}");
                    logger.LogError(ex, "Error connecting to the database.");
                }
            }

            return response;
        }
    }
}


