package com.gigaspaces.tutorials.common.dao.impl;

import com.gigaspaces.tutorials.common.dao.AbstractDAO;
import com.gigaspaces.tutorials.common.dao.AccountDataDAO;
import com.gigaspaces.tutorials.common.model.AccountData;
import org.springframework.stereotype.Repository;

@Repository
public class AccountDataDAOImpl extends AbstractDAO<AccountData> implements AccountDataDAO {
}
