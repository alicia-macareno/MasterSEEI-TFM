CREATE TABLE dbo.[ChildAccount]
(
    ChildAccountID INT IDENTITY(1,1) NOT NULL,
    LoginEmail NVARCHAR(254) NOT NULL,
    PasswordHash BINARY(64) NOT NULL,
	Salt uniqueidentifier NOT NULL,
    FirstName NVARCHAR(40) NULL,
    FirstLastName NVARCHAR(40) NULL,
	SecondLastName NVARCHAR(40),
	CreatedOn datetime NOT NULL,
	Status bit NOT NULL,
	Blocked bit NOT NULL,
	LastSuccessfulLogin datetime NULL,
	LastFailedLogin datetime NULL,
	FailedLoginAttempts int NULL,
	ParentAccount int REFERENCES dbo.ParentAccount(ParentAccountID)
    CONSTRAINT [PK_ChildAccount_ChildAccountID] PRIMARY KEY CLUSTERED (ChildAccountID ASC)
)