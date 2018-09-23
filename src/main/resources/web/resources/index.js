$(function(){
    var device1 = $('#device1');
    var view1 = $('#view1');
    var view2 = $('#view2');
    var script1 = $('#script1');
    var script2 = $('#script2');
    var script3 = $('#script3');
    var script4 = $('#script4');
    var statusName = $('#statusName');

    var logs = $('#logs');
    var states = $('#states');
    var components = $('#components');
    var editComponents = $('#edit-components');
    var editScreens = $('#edit-screens');

    var editForm = $('#edit-form');

    var playerLevel = $('#player-level');
    var deviceIp = $('#device-ip');
    var deviceAdb = $('#device-adb');
    var deviceWifi = $('#device-wifi');
    var deviceDirect = $('#device-direct');

    var playPauseButton = $('#controlPlayPause');
    var unloadButton = $('#controlUnload');
    var killButton = $('#controlKill');
    var unloadEdit = $('#unloadEdit');


    var controlPlayerSave = $('#controlPlayerSave');
    var controlDeviceSave = $('#controlDeviceSave');
    var controlDeviceDirectSave = $('#controlDeviceDirectSave');
    var controlDeviceAdbSave = $('#controlDeviceAdbSave');
    var controlDeviceWifiSave = $('#controlDeviceWifiSave');

    var settingForm = $('#setting-form');
    var playerForm = $('#player-form');
    var variableForm = $('#variable-form');
    var variableContainer = $('#variable-container');
    var loadedForm = $('#loaded-form');

    var controlAdb = $('.controlAdb');
    var adbInfo = $('#adb-info');
    var linkedVariables = {};

    var notWhileRunning = $('.notWhileRunning');

    var logItems = [];

    var configList = $('#config-list');

    var configs = [];

    var settings = {};

    function populateSelect(select, items) {
        var i = 0;
        select.empty();
        for (i = 0; i < items.length; i++) {
            select.append($('<option></option>').text(items[i]).attr('value', items[i]));
        }
    }

    view1.change(function(){
        view2.prop('disabled', !view1.val());
    });

    script1.change(function(){
        script2.prop('disabled', !script1.val());
    });

    script2.change(function(){
        script3.prop('disabled', !script2.val());
    });

    script3.change(function(){
        script4.prop('disabled', !script3.val());
    });

    function createGroup(name, field, button) {
        var grp = $('<div class="input-group mb-3"></div>').appendTo(variableContainer);
        $('<div class="input-group-prepend"></div>').append($('<span class="input-group-text" id=""></span>').text(name)).appendTo(grp);
        field.appendTo(grp);
        $('<div class="input-group-append"></div>').append(button).appendTo(grp);
        return grp;
    }

    $('#controlUpdatePreview').click(function(){
        $('#previewImage').attr('src', '/piper/screen?time=' + (new Date().getTime()));
    });

    function statusCheck(firstTime) {
        $.getJSON({
            url: '/piper/status',
            data: {},
            success: function(data){

                var i = 0, item, div;

                configList.hide();

                statusName.text(data.status);

                switch(data.status) {
                        case 'EDIT_VIEW':
                        case 'READY': {
                    $('.whenLoaded').prop('disabled', false);
                   } break;
                   default: {
                    $('.whenLoaded').prop('disabled', true);
                   }
                }

                switch(data.status) {
                    case 'EDIT_VIEW': {
                        $('.whenEdit').show();
                    } break;
                    default: {
                        $('.whenEdit').hide();
                    } break;
                }

                switch(data.status) {
                    case 'EDIT_VIEW':
                    case 'READY':
                    case 'RUNNING':
                    case 'STOPPING':
                    case 'STOPPED':
                     {
                        $('.whenLoaded').show();
                        $('.notLoaded').hide();
                    } break;
                    default: {
                        $('.whenLoaded').hide();
                        $('.notLoaded').show();
                    } break;
                }

                switch(data.status) {
                    case 'READY':
                    case 'RUNNING':
                    case 'STOPPING':
                    case 'STOPPED':
                     {
                        $('.whenRun').show();
                    } break;
                    default: {
                        $('.whenRun').hide();
                    } break;
                }


                switch(data.status) {
                    case 'EDIT_VIEW': {
                        editForm.show();
                        settingForm.hide();
                        if (firstTime) {
                            $('[href="#edit"]').tab('show');
                            loadEditViewInfo(true);
                        }
                    } break;
                    case 'INIT': {
                        editForm.hide();
                        playPauseButton.addClass('disabled');
                        unloadButton.addClass('disabled');
                        settingForm.show();
                        loadedForm.hide();
                        states.prop('disabled', true);
                        configList.show();
                    } break;
                    case 'READY': {
                        if (firstTime) {
                            $('[href="#run"]').tab('show');
                            loadProcessInfo(true);
                        } else {
                            editForm.hide();
                            playPauseButton.removeClass('disabled');
                            unloadButton.removeClass('disabled');
                            settingForm.hide();
                            loadedForm.show();
                            states.prop('disabled', false);
                            notWhileRunning.prop('disabled', false);
                        }
                    } break;
                    case 'RUNNING':
                    case 'STOPPING':
                    case 'STOPPED': {
                        if (firstTime) {
                            $('[href="#run"]').tab('show');
                            loadProcessInfo(true);
                        } else {
                            editForm.hide();
                            playPauseButton.removeClass('disabled');
                            unloadButton.removeClass('disabled');
                            settingForm.hide();
                            loadedForm.show();
                            states.prop('disabled', true);
                            notWhileRunning.prop('disabled', true);
                            setTimeout(statusCheck, 3000);
                        }
                    } break;
                }

                if (data.state) {
                    states.val(data.state);
                }

                for (i = 0; i < data.logs.length; i++) {
                    item = data.logs[i];
                    div = $('<div class="row"></div>')
                    div.append($('<div class="col-sm-2 col-md-2 d-none d-sm-block"></div>').text(item.source));
                    div.append($('<div class="col-sm-4 col-md-2"></div>').text(item.timestamp));
                    div.append($('<div class="col-sm-2 col-md-2 d-none d-sm-block"></div>').text(item.level));
                    div.append($('<div class="col-sm-8 col-md-6"></div>').text(item.message));
                    logItems.push(div);
                    logs.prepend(div);
                }

                while (logItems.length > 50) {
                    logItems.splice(0,1)[0].detach();
                }

                if (data.variables.length > 0) {
                    for (i = 0; i < data.variables.length; i++) {
                        item = data.variables[i];
                        linkedVariables[item.name].val(formatVariable(item));
                    }
                }

            }
        });
    }

    $(".logging").change(function(){
        var ref = $(this), level = ref.val(), mode = ref.attr('mode');
        $.ajax({
            type: "POST",
            url: '/piper/process/level/' + mode + '/' + level,
            success: function(result){

            }
        });
    });

    playPauseButton.click(function(){
        if (!playPauseButton.hasClass('disabled')) {
            $.ajax({
              type: "POST",
              url: '/piper/process/playPause/' + states.val(),
              success: function(result){
                statusCheck();
              }
            });
        }
    });

    unloadButton.click(function(){
        if (!unloadButton.hasClass('disabled')) {
            $.ajax({
              type: "POST",
              url: '/piper/process/unload',
              success: function(result){
                statusCheck();
                $('[href="#config"]').tab('show');
              }
            });
        }
    });

    killButton.click(function(){
        if (!unloadButton.hasClass('disabled')) {
            $.ajax({
                type: "POST",
                url: '/piper/process/kill',
                success: function(result){
                    statusCheck();
                    $('[href="#config"]').tab('show');
                }
            });
        }
    });

    unloadEdit.click(function(){
        if (!unloadEdit.hasClass('disabled')) {
            $.ajax({
              type: "POST",
              url: '/piper/edit/unload',
              success: function(result){
                statusCheck();
                $('[href="#config"]').tab('show');
              }
            });
        }
    });

    controlPlayerSave.click(function(){
        $.ajax({
          type: "POST",
          url: '/piper/player/level',
          data: {value: playerLevel.val()}
        });
    });

    controlAdb.click(function(){
        adbInfo.val("Please Wait...");
        $.ajax({
            type: "POST",
            url: '/piper/adb/' + $(this).attr('adb'),
            data: {},
            success: function(result){
                adbInfo.val(result.value || 'Done');
            }
        });
    });

    controlDeviceSave.click(function(){
        $.ajax({
          type: "POST",
          url: '/piper/device/ip',
          data: {value: deviceIp.val()}
        });
    });

    controlDeviceDirectSave.click(function(){
        $.ajax({
          type: "POST",
          url: '/piper/device/direct',
          data: {value: deviceDirect.val()}
        });
    });

    controlDeviceAdbSave.click(function(){
        $.ajax({
          type: "POST",
          url: '/piper/device/adb',
          data: {value: deviceAdb.val()}
        });
    });

    controlDeviceWifiSave.click(function(){
        $.ajax({
          type: "POST",
          url: '/piper/device/wifi',
          data: {value: deviceWifi.val()}
        });
    });

    $('.controlButton').click(function(){
        var ref= $(this), button = ref.attr('controlButton'), componentId = $('#components').val();
        $.ajax({
            type: "POST",
            url: '/piper/button',
            data: {buttonId: button, componentId: componentId}
        });
    });

    configList.on('click', 'button', function(){
        var ref = $(this), index = ref.attr('index') - 0;
        if (index >= 0 && index < configs.length) {
            if (ref.hasClass('run-config')) {
                applySelection(configs[index]);
                loadButton.click();
            } else if (ref.hasClass('delete-config')) {
                configs.splice(index, 1);
                localStorage.setItem("CONFIGS", JSON.stringify(configs));
                buildConfigs();
            } else if (ref.hasClass('modify-config')) {
                applySelection(configs[index]);
                $('[href="#config"]').tab('show');
            }
        }
    });


    function doubleWide(numb) {
        if (numb < 10) {
            return "0" + numb;
        }
        return numb;
    }

    function formatVariable(varDef) {
        switch(varDef.displayType) {
            case 'STANDARD':
                return varDef.value;
            case 'TENTH': {
                return ((varDef.value - 0) / 10.0).toFixed(1) + "%";
            } break;
            case 'BOOLEAN': {
                return varDef.value - 0;
            } break;
            case 'SECONDS': {
                var total = varDef.value - 0;
                var hours = Math.floor(total / (3600));
                total -= (hours * 3600);
                var minutes = Math.floor(total / 60);
                var seconds = total % 60;
                return doubleWide(hours) + ":" + doubleWide(minutes) + ":" + doubleWide(seconds);
            } break;
        }
    }

    function loadProcessInfo(firstTime) {
        $.ajax({
          type: "GET",
          url: '/piper/process/info',
          success: function(result){
            if (result.status == 'OK') {
                states.empty();
                components.empty();
                var i, item, grp, label, input, button;
                for (i = 0; i < result.states.length; i++) {
                states.append($('<option></option>').attr('value', result.states[i].value).text(result.states[i].name).attr('description', result.states[i].description));
                }
                for (i = 0; i < result.components.length; i++) {
                components.append($('<option></option>').attr('value', result.components[i].value).text(result.components[i].name));
                }
                $('#webLogging').val(result.webLevel);
                $('#fileLogging').val(result.fileLevel);
                variableContainer.empty();
                linkedVariables = {};
                if (result.variables.length > 0) {
                    variableForm.show();

                    var row;

                    for (i = 0; i < result.variables.length; i++) {
                        item = result.variables[i];

                        if (i == 0) {
                            row = $('<div class="row"></div>').appendTo(variableContainer);
                        }

                        grp = $('<div class="input-group col-sm-12 col-md-6 col-lg-4" style="margin-bottom: 1em"></div>').appendTo(row);

                        $('<div class="input-group-prepend"></div>').append($('<span class="input-group-text" id=""></span>').text(item.display)).appendTo(grp);

                        if (item.displayType == 'BOOLEAN') {
                            linkedVariables[item.name] = $('<select class="form-control notWhileRunning"><option value="0">False</option><option value="1">True</option></select>')
                        } else {
                            linkedVariables[item.name] = $('<input type="text" class="form-control notWhileRunning"/>');
                        }

                        linkedVariables[item.name].data('key', item.name).val(formatVariable(item)).attr('id', 'var_' + item.name).appendTo(grp);
                        switch(item.modify) {
                            case 'EDITABLE': {
                                $('<div class="input-group-append"></div>').append($('<button class="btn btn-secondary updateVariable notWhileRunning" type="button">Update</button>').data('key', item.name).attr('for', 'var_' + item.name)).appendTo(grp);
                            } break;
                        }
                    }
                } else {
                    variableForm.hide();
                }
                statusCheck();
            }
          },
          dataType: 'json'
        });
    }

    function loadEditViewInfo(firstTime) {
        $.ajax({
          type: "GET",
          url: '/piper/edit/view/info',
          success: function(result){
            if (result.status == 'OK') {
                editScreens.empty();
                editComponents.empty();
                var i, item, grp, label, input, button;
                for (i = 0; i < result.screens.length; i++) {
                    editScreens.append($('<option></option>').attr('value', result.screens[i].value).text(result.screens[i].name));
                }
                for (i = 0; i < result.components.length; i++) {
                    editComponents.append($('<option></option>').attr('value', result.components[i].value).text(result.components[i].name));
                }
                if (firstTime) {
                    statusCheck();
                }
            }
          },
          dataType: 'json'
        });
    }

    variableContainer.on('change', 'select, input', function(){
        var ref = $(this);
        ref.addClass('updatedValue');
    });

    variableContainer.on('click', 'button.updateVariable', function(){
        var ref = $(this), key = ref.data('key'), input = linkedVariables[key];
        input.removeClass('updatedValue');
        if (input.val()) {
            $.ajax({
              type: "POST",
              url: '/piper/variable',
              data: {key: key, value: input.val()}
            });
        }
    });


    ///////////////////////////////////////////////////////////////////////////
    // EDIT
    ///////////////////////////////////////////////////////////////////////////

    $('.edit-action').click(function(){
        var ref = $(this), action = ref.attr('editvalue'), list = $('#' + ref.attr('lst')), id = list.val();
        $.ajax({
          type: "POST",
          url: '/piper/edit/action/' + action + "/" + id + '/' + 'null',
          success: function(result){
            if (result.msg) {
                alert(result.msg);
            }
          },
          dataType: 'json'
        });

    });


    ///////////////////////////////////////////////////////////////////////////
    // Configuration
    ///////////////////////////////////////////////////////////////////////////

    var loadButton = $('#controlLoad');
    var saveButton = $('#controlSave');
    var editViewButton = $('#controlEditView');

    function createConfigItem(before, title, cssClazz, data) {
        var wrap = $('<div class="input-group extra-field"></div>');
        wrap.append($('<div class="input-group-prepend"></div>').append($('<span class="input-group-text">Title</span>').text(title)));
        var select = $('<select class="form-control"></select>').addClass(cssClazz).appendTo(wrap);
        populateSelect(select, data || []);
        wrap.append($('<div class="input-group-append"></div>').append($('<button type="button" class="btn btn-danger remove">X</button>').data('row', wrap)));
        $('<br class="extra-field"/>').insertBefore(before);
        wrap.insertBefore(before);
        return select;
    }

    function resetConfigPage() {
        var configForm = $('#config');

        $('#configName').val('');


        // Remove extra fields
        $('.extra-field', configForm).remove();

        populateSelect($('#device1'), settings.devices || []);

        $('.view-item').each(function(){
            populateSelect($(this), settings.views || []);
        });

        $('.script-item').each(function(){
            populateSelect($(this), settings.scripts || []);
        });

        $('.config-attribute', configForm).val('');
    }

    function extractConfig() {

        var title = $('#configName').val();

        var device1 = $('#device1').val();
        var viewList = [];
        $('.view-item').each(function(){
            var ref = $(this);
            if (ref.val()) {
                viewList.push(ref.val());
            }
        });
        var scriptList = [];
        $('.script-item').each(function(){
            var ref = $(this);
            if (ref.val()) {
                scriptList.push(ref.val());
            }
        });

        var attributes = {};
        $('.config-attribute').each(function(){
            var ref = $(this), key = ref.attr('attrname'), val = ref.val();
            if (val) {
                attributes[key] = val;
            }
        });

        var data = {
            device: device1 || '',
            views: viewList,
            scripts: scriptList,
            title: title,
            attributes: attributes
        };

        return data;
    }

    function saveConfig() {

        var data = extractConfig();

        if (!data.title) {
            alert('Please provide a name');
            return;
        }

        var i = 0, found = false;
        for (i = 0; i < configs.length; i++) {
            if (configs[i].title == title) {
                found = true;
                break;
            }
        }

        if (found) {
            configs.splice(i, 1, data);
        } else  {
            configs.push(data);
        }

        localStorage.setItem("CONFIGURATIONS", JSON.stringify(configs));

        buildConfigs();
    }

    function applySelection(j) {

        resetConfigPage();

        $('#configName').val(j.title || '');

        // Device
        var device1 = $('#device1').val('');
        if (j.device) device1.val(j.device);

        // Views
        var viewList = (j.views || []);
        var i = 0, k = 0, item;
        var view1 = $('.view-item').val('');
        for (i = 0; i < viewList.length; i++) {
            item = $.trim(viewList[i]);
            if (item) {
                if (k == 0) {
                    view1.val(item);
                } else {
                    createConfigItem($('#addViewHolder'), 'View', 'view-item', settings.views).val(item);
                }
                k++;
            }
        }

        // Scripts
        var scriptList = (j.scripts || []);
        i = 0;
        k = 0;
        var script1 = $('.script-item').val('');
        for (i = 0; i < scriptList.length; i++) {
            item = $.trim(scriptList[i]);
            if (item) {
                if (k == 0) {
                    script1.val(item);
                } else {
                    createConfigItem($('#addScriptHolder'), 'Script', 'script-item', settings.scripts).val(item);
                }
                k++;
            }
        }

        // Attributes
        var attributeMap = (j.attributes || {});
        for (var key in attributeMap) {
            // check if the property/key is defined in the object itself, not in parent
            if (attributeMap.hasOwnProperty(key)) {
                $('.config-attribute[attrname='+key+']').val(attributeMap[key]);
            }
        }
    }

    $('#addViewButton').click(function(){
        createConfigItem($('#addViewHolder'), 'View', 'view-item', settings.views)
    });

    $('#addScriptButton').click(function(){
        createConfigItem($('#addScriptHolder'), 'Script', 'script-item', settings.scripts)
    });

    $('#config').on('click', '.remove', function(){
        $(this).data('row').remove();
    });

    saveButton.click(function(){
        if (!saveButton.hasClass('disabled')) {
            saveConfig();
        }
    });

    loadButton.click(function(){
    var blob = extractConfig();
    $.ajax({
      type: "POST",
      url: '/piper/process/prep',
      data: JSON.stringify(blob),
      headers: {
            'Content-Type': 'application/json'
        },
      success: function(result){
        if (result.status == 'OK') {
            loadProcessInfo(true);
            $('[href="#run"]').tab('show');
        }
      },
      dataType: 'json'
    });
});

editViewButton.click(function(){
    var blob = extractConfig();
    $.ajax({
      type: "POST",
      url: '/piper/edit/view',
      data: JSON.stringify(blob),
      headers: {
            'Content-Type': 'application/json'
        },
      success: function(result){
        if (result.status == 'OK') {
            loadEditViewInfo(true);
            $('[href="#edit"]').tab('show');
        }
      },
      dataType: 'json'
    });

});

    ///////////////////////////////////////////////////////////////////////////
    // Home
    ///////////////////////////////////////////////////////////////////////////

    function buildConfigs() {
        var i;
        $('.customConfig',configList).remove();
        for (i = 0; i < configs.length; i++) {
            createHomeItem(i, configs[i].title)
        }
    }

    function createHomeItem(index, title) {
        var wrap, card, body, bntGroup;
        wrap = $('<div class="col-sm-12 col-md-6 col-lg-4 col-xl-3 customConfig"></div>');
        card = $('<div class="card shadow-sm"></div>');
        $('<div class="card-header"></div>').append($('<h4 class="my-0 font-weight-normal"></h4>').text(title || 'Untitled')).appendTo(card);
        body = $('<div class="card-body"></div>').appendTo(card);
        bntGroup = $('<div class="btn-group" role="group" aria-label="Controls"></div>').appendTo(body);
        bntGroup.append($('<button type="button" class="btn run-config btn-primary">Run</button>').attr('index', index));
        bntGroup.append($('<button type="button" class="btn modify-config btn-outline-dark">Modify</button>').attr('index', index));
        bntGroup.append($('<button type="button" class="btn delete-config btn-outline-danger">Delete</button>').attr('index', index));
        wrap.append(card);
        configList.append(wrap);
    }

    function loadConfigurations() {
        var configData = localStorage.getItem("CONFIGURATIONS");
        if (configData) {
            configData = JSON.parse(configData);
            configs = [];
            var i;
            for (i = 0; i < configData.length; i++) {
                configs.push(configData[i]);
            }
            buildConfigs();
        }
    }

    $('#new-config').click(function(){
        resetConfigPage();
        $('[href="#config"]').tab('show');
    });

    ///////////////////////////////////////////////////////////////////////////
    // First Time
    ///////////////////////////////////////////////////////////////////////////

    function firstTimeLoad() {
        $.getJSON({
          url: '/piper/settings/list',
          data: {},
          success: function(data){
            settings = data;
            resetConfigPage();
            loadConfigurations();
            statusCheck(true);
          }
        });

        $.getJSON({
              url: '/piper/player/level',
              data: {},
              success: function(data){
                playerLevel.val(data.value);
                playerForm.show();
              }
          }
        );

    }

    setTimeout(firstTimeLoad, 100);
});