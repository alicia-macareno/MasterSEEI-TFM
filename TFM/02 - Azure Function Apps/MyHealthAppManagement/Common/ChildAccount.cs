using System;

namespace MyHealthAppManagement.Common
{
    internal class ChildAccount
    {
        public int ChildAccountID { get; set; }
        public string LoginEmail { get; set; }
        public string FirstName { get; set; }
        public string FirstLastName { get; set; }
        public string SecondLastName { get; set; }
        public bool Status { get; set; }
        public bool Blocked { get; set; }
        public int FailedLoginAttempts { get; set; }
        public DateTime CreatedOn { get; set; }
        public bool RealTimeMonitoring { get; set; }
        public int Perimeter { get; set; }
        public bool PendingLocationConfig { get; set; }
    }
}
