require("module-alias/register");
require('dotenv').config();

const Conversation = require('./Conversation');
const File = require('./File');
const Platform = require('./Platform');
const Model = require('./Model');
const DefaultModelSettingTable = require('./DefaultModelSetting');
const SearchProviderTable = require('./SearchProvider');
const UserProviderConfigTable = require('./UserProviderConfig');
const UserSearchSettingTable = require('./UserSearchSetting');
const LLMLogs = require('./LLMLogs');
const Task = require('./Task');
const Message = require('./Message');
const McpServer = require('./McpServer');
const Agent = require('./Agent');
const FileVersion = require('./FileVersion');
const Knowledge = require('./Knowledge');
const User = require('./User');

const tableSync = async () => {
  const models = [
    Conversation, File, Platform, Model, DefaultModelSettingTable,
    SearchProviderTable, UserProviderConfigTable, UserSearchSettingTable,
    LLMLogs, Task, Message, McpServer, Agent, FileVersion, Knowledge, User
  ];

  for (const model of models) {
    try {
      await model.sync({ alter: true });
    } catch (error) {
      console.log(`Table sync failed for ${model.name}, trying fresh creation:`, error.message);
      try {
        await model.sync({ force: true });
      } catch (forceError) {
        console.error(`Failed to create table for ${model.name}:`, forceError.message);
      }
    }
  }
}

const dataSync = async () => {
  const count = await Platform.count();
  if (count === 0) {
    const defaultData = require('../../public/default_data/default_platform.json');
    for (const item of defaultData) {
      const platformData = {
        name: item.name,
        logo_url: item.logo_url,
        source_type: 'system',
        api_key: item.api_key,
        api_url: item.api_url,
        api_version: item.api_version,
        key_obtain_url: item.key_obtain_url,
        is_subscribe: item.is_subscribe || false
      };
      const platform = await Platform.create(platformData);

      const modelsData = item.models.map(model => ({
        // @ts-ignore
        platform_id: platform.id,
        logo_url: model.logo_url,
        model_id: model.model_id,
        model_name: model.model_name,
        group_name: model.group_name,
        model_types: model.model_types,
      }));
      await Model.bulkCreate(modelsData);
    }
  }

  const searchProviderCount = await SearchProviderTable.count();
  if (searchProviderCount === 0) {
    const defaultSearchProviderData = require('../../public/default_data/default_search_provider.json');
    for (const item of defaultSearchProviderData) {
      const searchProviderData = {
        name: item.name,
        logo_url: item.logo_url,
        base_config_schema: item.base_config_schema,
      };
      await SearchProviderTable.create(searchProviderData);
    }
  }

  const userCount = await User.count();
  if (userCount === 0) {
    await User.create({
      id: 1,
      user_salt: 'default123'
    });
  }
}

const dataUpdate = async () => {
  const defaultData = require('../../public/default_data/default_platform.json');

  // v0.1 => v0.1.1
  await Platform.update({
    api_url: 'https://ark.cn-beijing.volces.com/api/v3'
  }, {
    where: {
      name: 'Volcengine'
    }
  })
  
  // Remove Gemini platform and add Puter platform instead
  // First, remove any existing Gemini platform
  await Platform.destroy({ where: { name: 'Gemini' } });
  
  // Check if Puter platform exists, if not create it
  const puterPlatform = await Platform.findOne({ where: { name: 'Puter' } })
  if (!puterPlatform) {
    // Create Puter platform with default models
    const platformData = {
      name: 'Puter',
      logo_url: 'https://res.cloudinary.com/ddz3nsnq1/image/upload/v1760460731/putersvg_yqrokx.svg',
      source_type: 'system',
      api_key: '', // Puter doesn't require API key as it uses user's auth
      api_url: 'https://api.puter.com', // Puter API URL (even though not used for chat, it's needed for other services)
      api_version: 'v2', // Puter API version
      key_obtain_url: 'https://puter.com', // Puter website for obtaining access
      is_enabled: true, // Enable the platform by default
      is_subscribe: false, // Puter doesn't require subscription
    };
    const platform = await Platform.create(platformData);
    
    // Create default Puter models
    const modelsData = [
      {
        // @ts-ignore
        platform_id: platform.id,
        logo_url: 'https://res.cloudinary.com/ddz3nsnq1/image/upload/v1760460731/putersvg_yqrokx.svg',
        model_id: 'gpt-5-nano',
        model_name: 'GPT-5 Nano',
        group_name: 'GPT-5',
        model_types: ['text-generation'],
      },
      {
        // @ts-ignore
        platform_id: platform.id,
        logo_url: 'https://res.cloudinary.com/ddz3nsnq1/image/upload/v1760460731/putersvg_yqrokx.svg',
        model_id: 'gpt-5-mini',
        model_name: 'GPT-5 Mini',
        group_name: 'GPT-5',
        model_types: ['text-generation'],
      },
      {
        // @ts-ignore
        platform_id: platform.id,
        logo_url: 'https://res.cloudinary.com/ddz3nsnq1/image/upload/v1760460731/putersvg_yqrokx.svg',
        model_id: 'claude-sonnet-4',
        model_name: 'Claude Sonnet 4',
        group_name: 'Claude',
        model_types: ['text-generation'],
      },
      {
        // @ts-ignore
        platform_id: platform.id,
        logo_url: 'https://res.cloudinary.com/ddz3nsnq1/image/upload/v1760460731/putersvg_yqrokx.svg',
        model_id: 'gemini-2.0-flash',
        model_name: 'Gemini 2.0 Flash',
        group_name: 'Gemini',
        model_types: ['text-generation'],
      }
    ];
    await Model.bulkCreate(modelsData);
  } else {
    // Update existing Puter platform to ensure correct settings
    await Platform.update({
      logo_url: 'https://res.cloudinary.com/ddz3nsnq1/image/upload/v1760460731/putersvg_yqrokx.svg',
      api_key: '',
      api_url: 'https://api.puter.com',
      api_version: 'v2',
      key_obtain_url: 'https://puter.com',
      is_enabled: true,
      is_subscribe: false,
    }, {
      where: { name: 'Puter' }
    });
  }

  // v0.1.1 => v0.1.2
  const defaultSearchProviderData = require('../../public/default_data/default_search_provider.json');
  const CloudswaySearchProvider = defaultSearchProviderData.find(item => item.name === 'Cloudsway');
  const searchProvider = await SearchProviderTable.findOne({ where: { name: CloudswaySearchProvider.name } });
  if (!searchProvider) {
    const searchProviderData = {
      name: CloudswaySearchProvider.name,
      logo_url: CloudswaySearchProvider.logo_url,
      base_config_schema: CloudswaySearchProvider.base_config_schema,
    };
    await SearchProviderTable.create(searchProviderData);
  }

  const cloudswayPlatform = await Platform.findOne({ where: { name: 'Cloudsway' } })
  if (!cloudswayPlatform) {
    const cloudswayPlatform = defaultData.find(item => item.name === 'Cloudsway')
    const platformData = {
      name: cloudswayPlatform.name,
      logo_url: cloudswayPlatform.logo_url,
      source_type: 'system',
      api_key: cloudswayPlatform.api_key,
      api_url: cloudswayPlatform.api_url,
      api_version: cloudswayPlatform.api_version,
      key_obtain_url: cloudswayPlatform.key_obtain_url,
    };
    const platform = await Platform.create(platformData);
    const modelsData = cloudswayPlatform.models.map(model => ({
      // @ts-ignore
      platform_id: platform.id,
      logo_url: model.logo_url,
      model_id: model.model_id,
      model_name: model.model_name,
      group_name: model.group_name,
      model_types: model.model_types,
    }));
    await Model.bulkCreate(modelsData);
  }
  // v0.1.2 => v0.1.3
  const platform_lemon = await Platform.findOne({ where: { name: 'Lemon' } })
  if (!platform_lemon) {
    const lemonPlatform = defaultData.find(item => item.name === 'Lemon')
    const platformData = {
      name: lemonPlatform.name,
      logo_url: lemonPlatform.logo_url,
      source_type: 'system',
      api_key: lemonPlatform.api_key,
      api_url: lemonPlatform.api_url,
      api_version: lemonPlatform.api_version,
      key_obtain_url: lemonPlatform.key_obtain_url,
      is_subscribe: true,
      is_enabled: true
    };
    const platform = await Platform.create(platformData);
    const modelsData = lemonPlatform.models.map(model => ({
      // @ts-ignore
      platform_id: platform.id,
      logo_url: model.logo_url,
      model_id: model.model_id,
      model_name: model.model_name,
      group_name: model.group_name,
      model_types: model.model_types,
    }));
    await Model.bulkCreate(modelsData);
  }

  // v0.1.3 => v0.1.4
  await Platform.update({ is_enabled: true }, { where: { name: 'Lemon' } })
  SearchProviderTable.destroy({ where: { name: 'Baidu' } });
  SearchProviderTable.destroy({ where: { name: 'Bing' } });
}

const sync = async () => {
  try {
    await tableSync();
    await dataSync();
    await dataUpdate();
  } catch (error) {
    console.error('Error during sync:', error);
  }
}

sync()

module.exports = exports = sync;