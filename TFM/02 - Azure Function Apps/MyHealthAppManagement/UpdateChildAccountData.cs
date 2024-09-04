namespace MyHealthAppManagement
{
    using System;
    using System.Data;
    using Microsoft.Data.SqlClient;
    using System.IO;
    using System.Threading.Tasks;
    using Microsoft.AspNetCore.Mvc;
    using Microsoft.Azure.WebJobs;
    using Microsoft.Azure.WebJobs.Extensions.Http;
    using Microsoft.AspNetCore.Http;
    using Microsoft.Extensions.Logging;
    using Newtonsoft.Json;

    public static class UpdateChildAccountData
    {
        [FunctionName("UpdateChildAccountData")]
        public static async Task<IActionResult> Run(
            [HttpTrigger(AuthorizationLevel.Function, "post", Route = null)] HttpRequest req,
            ILogger log)
        {
            log.LogInformation("C# HTTP trigger function processed a request.");

            // Parse request body
            string requestBody = await new StreamReader(req.Body).ReadToEndAsync();
            dynamic data = JsonConvert.DeserializeObject(requestBody);

            // Retrieve parameters from the request body
            string loginEmail = data?.loginEmail;
            string newPassword = data?.newPassword;
            string newFirstName = data?.newFirstName;
            string newFirstLastName = data?.newFirstLastName;
            string newSecondLastName = data?.newSecondLastName;
            int? newStatus = data?.newStatus;
            bool? newBlocked = data?.newBlocked;
            int? newFailedLoginAttempts = data?.newFailedLoginAttempts;
            bool? newRealTimeMonitoring = data?.newRealTimeMonitoring;
            int? newPerimeter = data?.newPerimeter;
            bool? newPendingLocationConfig = data?.newPendingLocationConfig;

            if (string.IsNullOrEmpty(loginEmail))
            {
                return new BadRequestObjectResult("LoginEmail cannot be null or empty");
            }

            var connectionString = Environment.GetEnvironmentVariable("AZURE_SQL_CONNECTIONSTRING");

            try
            {
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();
                    log.LogInformation("Successful connection to DB!");

                    using (SqlCommand command = new SqlCommand("dbo.uspUpdateChildAccount", conn))
                    {
                        command.CommandType = CommandType.StoredProcedure;

                        // Add parameters to the command
                        command.Parameters.Add(new SqlParameter("@pLoginEmail", SqlDbType.NVarChar) { Value = loginEmail });
                        command.Parameters.Add(new SqlParameter("@pNewPassword", SqlDbType.NVarChar) { Value = (object)newPassword ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewFirstName", SqlDbType.NVarChar) { Value = (object)newFirstName ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewFirstLastName", SqlDbType.NVarChar) { Value = (object)newFirstLastName ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewSecondLastName", SqlDbType.NVarChar) { Value = (object)newSecondLastName ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewStatus", SqlDbType.Int) { Value = (object)newStatus ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewBlocked", SqlDbType.Bit) { Value = (object)newBlocked ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewFailedLoginAttempts", SqlDbType.Int) { Value = (object)newFailedLoginAttempts ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewRealTimeMonitoring", SqlDbType.Bit) { Value = (object)newRealTimeMonitoring ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewPerimeter", SqlDbType.Int) { Value = (object)newPerimeter ?? DBNull.Value });
                        command.Parameters.Add(new SqlParameter("@pNewPendingLocationConfig", SqlDbType.Bit) { Value = (object)newPendingLocationConfig ?? DBNull.Value });

                        // Output parameters
                        SqlParameter responseMessageParam = new SqlParameter("@responseMessage", SqlDbType.NVarChar, 250)
                        {
                            Direction = ParameterDirection.Output
                        };
                        SqlParameter responseCodeParam = new SqlParameter("@responseCode", SqlDbType.Int)
                        {
                            Direction = ParameterDirection.Output
                        };
                        command.Parameters.Add(responseMessageParam);
                        command.Parameters.Add(responseCodeParam);

                        // Execute the command
                        await command.ExecuteNonQueryAsync();

                        // Get response message and code
                        string responseMessage = responseMessageParam.Value.ToString();
                        int responseCode = (int)responseCodeParam.Value;

                        // Return response based on response code
                        if (responseCode == 200)
                        {
                            return new OkObjectResult(responseMessage);
                        }
                        else if (responseCode == 404)
                        {
                            return new NotFoundObjectResult(responseMessage);
                        }
                        else
                        {
                            return new ConflictObjectResult(responseMessage);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                log.LogError($"Exception: {ex.Message}");
                return new StatusCodeResult(StatusCodes.Status500InternalServerError);
            }
        }
    }
}
