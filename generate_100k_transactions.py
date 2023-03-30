import csv
import random
from faker import Faker

headers = [["idBank", 'name', 'local'], # bank
           ['idCustomer', 'firstName', 'lastName'], # customer
           ['Bank_idBank', 'Customer_idCustomer', 'hashedPin', 'local_transfer_limit', 'overseas_transfer_limit'], # bank_has_customer
           ['idAccount', 'Customer_idCustomer', 'Bank_idBank', 'name', 'balance'], # account
           ['idTransaction', 'Account_idAccount', 'amount', 'timeStamp', 'receiverID', 'memo','local']] # transaction
# Fake Banks
banks = ["Heng Bank", "Hong Bank", "Super Overseas Bank"]
locals= [1,1,0]
account_names = ["Savings", "Checkings", "Retirement", "Holiday", "Spending"]
rows = []
memos = ["Instalment", "Insurance", "Withdraw", "Transfer", "Food", "Online Shopping"]

#Fake customer
# Create a faker object
fake = Faker()

# Define the number of customers to generate
num_customers = 100

# Generate random first and last names for the specified number of customers
first_names = [fake.first_name() for _ in range(num_customers)]
last_names = [fake.last_name() for _ in range(num_customers)]

# Fake accounts
account_names_counter = 0
account_id = 0
getBankID = {}
with open('accounts.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(headers[3])
    for i in range(num_customers):
        rand_bank = random.randint(0, len(banks) - 1)
        random_account_name = account_names[i % len(account_names)]
        row = [account_id, i, rand_bank, random_account_name,
               round(random.uniform(0.0, 10000.0), 2)]
        getBankID[account_id] = rand_bank
        writer.writerow(row)
        account_names_counter += 1
        account_id += 1

    # Add a new account for any customer that does not have one yet
    for i in range(num_customers):
        customer_has_account = False
        with open('accounts.csv', 'r') as f:
            reader = csv.reader(f)
            for row in reader:
                if int(row[1]) == i:
                    customer_has_account = True
                    break
        if not customer_has_account:
            rand_bank = random.randint(0, len(banks) - 1)
            random_account_name = account_names[i % len(account_names)]
            row = [account_id, i, rand_bank, random_account_name,
                   round(random.uniform(0.0, 10000.0), 2)]
            getBankID[account_id] = rand_bank
            writer.writerow(row)
            account_id += 1



with open('bank.csv', 'w') as f:
    writer = csv.writer(f)
    writer.writerow(headers[0])
    for i, bank in enumerate(banks):
        row = [i, bank, locals[i]]
        writer.writerow(row)



# Write the customer data to a CSV file
with open('customers.csv', 'w') as f:
    writer = csv.writer(f)
    writer.writerow(headers[1])
    for i in range(num_customers):
        row = [i, first_names[i], last_names[i]]
        writer.writerow(row)

# Fake bank_has_customer
cust_has_bank = {}
with open('bank_has_customer.csv', 'w') as f:
    writer = csv.writer(f)
    writer.writerow(headers[2])
    for i in range(len(first_names)):
        random_bank = (i % len(banks))
        row = [random_bank, i, "03AC674216F3E15C761EE1A5E255F067953623C8B388B4459E13F978D7C846F4", 500, 1000]
        cust_has_bank[i] = random_bank
        writer.writerow(row)




# Fake accounts
account_names_counter = 0
account_id = 0
getBankID = {}
with open('accounts.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(headers[3])
    for i in range(len(first_names)):
        rand_bank = cust_has_bank[i]
        for num_accounts in range(random.randint(1, 3)):
            random_account_name = account_names[account_names_counter % len(account_names)]
            row = [account_id, i, rand_bank, random_account_name,
                   round(random.uniform(0.0, 10000.0), 2)]
            getBankID[account_id] = rand_bank
            writer.writerow(row)
            account_names_counter += 1
            account_id += 1

with open('transactions.csv', 'w') as f:
    writer = csv.writer(f)
    writer.writerow(headers[4])
    for i in range(100000):
        random_datetime = "202{}-{}-{} {}:{}:{}".format(random.randint(0, 3), random.randint(1, 12),
                                                        random.randint(1, 28), random.randint(0, 23),
                                                        random.randint(0, 59), random.randint(0, 59))

        random_account = random.randint(0, account_id - 1)
        random_sender_account = random_account
        random_receiver_account = random_account

        # while random_receiver_account == random_sender_account:
        #     random_receiver_account = random.randint(0, account_id - 1)
        random_amount = round(random.uniform(-1000.0, 1000.0), 2)
        bank_IDS = getBankID.get(random_sender_account)
        bank_IDR = getBankID.get(random_receiver_account)


        if (bank_IDS == 0 or bank_IDS == 1) and (bank_IDR == 0 or bank_IDR == 1):
            local = 1
        else:
            local = 0
        row = [i, random_account, random_amount, random_datetime, random_receiver_account,
               memos[random.randint(0, len(memos) - 1)], local]
        writer.writerow(row)




