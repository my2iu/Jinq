CONNECT 'jdbc:derby:demoDB;create=true';
DROP TABLE Customers;
CREATE TABLE Customers
(
	CustomerId	INTEGER NOT NULL,
	Name	VARCHAR(50) NOT NULL,
	Country VARCHAR(50) NOT NULL,
	Debt INTEGER NOT NULL,
	Salary INTEGER NOT NULL
);

DROP TABLE Sales;
CREATE TABLE Sales
(
	SaleId	INTEGER NOT NULL,
	CustomerId  INTEGER NOT NULL,
	Date VARCHAR(12) NOT NULL
);

DROP TABLE LineOrders;
CREATE TABLE LineOrders
(
	SaleId  INTEGER NOT NULL,
	ItemId  INTEGER NOT NULL,
	Quantity  INTEGER NOT NULL
);

DROP TABLE Items;
CREATE TABLE Items
(
	ItemId  INTEGER NOT NULL,
	Name  VARCHAR(50) NOT NULL,
	SalePrice DECIMAL NOT NULL,
	PurchasePrice DECIMAL NOT NULL
);

DROP TABLE ItemSuppliers;
CREATE TABLE ItemSuppliers
(
	ItemId  INTEGER NOT NULL,
	SupplierId  INTEGER NOT NULL
);

DROP TABLE Suppliers;
CREATE TABLE Suppliers
(
	SupplierId  INTEGER NOT NULL,
	Name  VARCHAR(50) NOT NULL,
	Country  VARCHAR(50) NOT NULL
);

INSERT INTO Customers (CustomerId, Name, Country)
VALUES 
	(1, 'ABB', 'Switzerland', 100, 200),
	(2, 'Nestle', 'Switzerland', 200, 300),
	(3, 'GM', 'USA', 300, 250),
	(4, 'Microsoft', 'USA', 100, 500),
	(5, 'Canadian Tire', 'Canada', 10, 30);
	
INSERT INTO Sales (SaleId, CustomerId, Date)
VALUES
	(1, 1, '2005'),
	(2, 1, '2004'),
	(3, 3, '2003'),
	(4, 3, '2004'),
	(5, 4, '2001'),
	(6, 5, '2005');
	
INSERT INTO LineOrders (SaleId, ItemId, Quantity)
VALUES
	(1, 1, 1),
	(2, 2, 2),
	(2, 5, 1),
	(2, 4, 2),
	(3, 5, 1000),
	(4, 1, 200),
	(5, 3, 6),
	(6, 1, 2),
	(6, 2, 2),
	(6, 4, 2),
	(6, 5, 7);
	
INSERT INTO Items (ItemId, Name, SalePrice, PurchasePrice)
VALUES
	(1, 'Widgets', 5, 10),
	(2, 'Wudgets', 2, 3),
	(3, 'Talent', 6, 1000),
	(4, 'Lawnmowers', 100, 102),
	(5, 'Screws', 1, 2);

INSERT INTO Suppliers (SupplierId, Name, Country)
VALUES
	(1, 'HW Supplier', 'Canada'),
	(2, 'Talent Agency', 'USA'),
	(3, 'Conglomerate', 'Switzerland');

INSERT INTO ItemSuppliers (ItemId, SupplierId)
VALUES
	(1, 1),
	(1, 3),
	(2, 1),
	(3, 2),
	(4, 3),
	(5, 1),
	(5, 2);
	