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
        public static async Task<HttpResponseMessage> Run([HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "ChildAccounts")] HttpRequestMessage req, Microsoft.Extensions.Logging.ILogger logger)
        {
            logger.LogInformation("CreateChild Azure API execution started...");
            HttpResponseMessage response = new HttpResponseMessage();
            // Get request body
            dynamic data = await req.Content.ReadAsAsync<object>();
            //Validate entry
            if (data.email == string.Empty || data.email == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Email cannot be empty nor null"); return response; }
            if (data.password == string.Empty || data.password == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Password cannot be empty nor null"); return response; }
            if (data.firstName == string.Empty || data.firstName == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("First name cannot be empty nor null"); return response; }
            if (data.firstLastName == string.Empty || data.firstLastName == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("First last name cannot be empty nor null"); return response; }
            if (data.parentAccountEmail == string.Empty || data.parentAccountEmail == null) { response.StatusCode = HttpStatusCode.BadRequest; response.Content = new StringContent("Parent account email cannot be empty nor null"); return response; }


            NewChildAccount accountData = new NewChildAccount();

            //Get account info from request
            accountData.Email = data?.email;
            accountData.Password = data?.password;
            accountData.firstName = data?.firstName;
            accountData.firstLastName = data?.firstLastName;
            accountData.secondLastName = data?.secondLastName;
            accountData.parentAccountEmail = data?.parentAccountEmail;

            var str = Environment.GetEnvironmentVariable("AZURE_SQL_CONNECTIONSTRING")!;

            using (SqlConnection conn = new SqlConnection(str))
            {
                try
                {
                    conn.Open();
                    logger.LogInformation("Successful connection to DB!");
                    //Set stored procedure
                    var command = new SqlCommand("dbo.uspAddChildAccount", conn);
                    command.CommandType = CommandType.StoredProcedure;
                    //Add procedure parameters
                    command.Parameters.Add(new SqlParameter("@pLoginEmail", accountData.Email));
                    command.Parameters.Add(new SqlParameter("@pPassword", accountData.Password));
                    command.Parameters.Add(new SqlParameter("@pFirstName", accountData.firstName));
                    command.Parameters.Add(new SqlParameter("@pFirstLastName", accountData.firstLastName));
                    command.Parameters.Add(new SqlParameter("@pSecondLastName", accountData.secondLastName));
                    command.Parameters.Add(new SqlParameter("@pParentAccountEmail", accountData.parentAccountEmail));
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