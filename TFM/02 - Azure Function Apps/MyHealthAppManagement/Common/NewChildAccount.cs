﻿using System;

namespace MyHealthAppManagement.Common
{
    
    internal class NewChildAccount
    {
        public string Email { get; set; }
        public string Password { get; set; }
        public string firstName { get; set; }
        public string firstLastName { get; set; }
        public string secondLastName { get; set; }
        public string parentAccountEmail { get; set; }
        public double latitude { get; set; }
        public double longitude { get; set; }
        public int perimeter { get; set; }
        public Boolean realTimeMonitoring { get; set; }
    }

}
