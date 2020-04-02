$(function(){
    var device1 = $('#device1');
    var view1 = $('#view1');
    var view2 = $('#view2');
    var script1 = $('#script1');
    var script2 = $('#script2');
    var script3 = $('#script3');
    var script4 = $('#script4');
    var statusName = $('#statusName');

    var bodyElement = $('body');

    var logs = $('#logs');
    var states = $('#states');
    var components = $('#components');
    var editComponents = $('#edit-components');
    editComponents.change(updateScreenComponentNames);
    var editScreens = $('#edit-screens');
    editScreens.change(updateScreenComponentNames);

    var editForm = $('#edit-form');

    var deviceIp = $('#device-ip');
    var deviceAdb = $('#device-adb');
    var deviceWifi = $('#device-wifi');
    var deviceDirect = $('#device-direct');

    var playPauseButton = $('.controlPlayPause');
    var intentButton = $('.controlIntent');
    var playButton = $('#playButton');
    var pauseButton = $('.pauseButton');
    var unloadButton = $('#controlUnload');
    var killButton = $('#controlKill');
    var dumpStateButton = $('#controlDumpState');
    var unloadEdit = $('#unloadEdit');

    var controlDeviceSave = $('#controlDeviceSave');
    var controlDeviceDirectSave = $('#controlDeviceDirectSave');
    var controlDeviceAdbSave = $('#controlDeviceAdbSave');
    var controlDeviceWifiSave = $('#controlDeviceWifiSave');

    var settingForm = $('#setting-form');
    var playerForm = $('#player-form');
    var variableForm = $('#variable-form');
    var variableContainer = $('#variable-container');
    var myTabContent = $('#myTabContent');
    //var loadedForm = $('#loaded-form');

    var controlAdb = $('.controlAdb');
    var adbInfo = $('#adb-info');
    var linkedVariables = {};

    var notWhileRunning = $('.notWhileRunning');

    var logItems = [];

    var configList = $('#config-list');
    var shortcutList = $('#shortcut-list');

    var configs = [];

    var settings = {};

    var viewSetup = {
        viewWidth:1080,
        viewHeight:1920,
        controlWidth:1080,
        controlHeight:1920
    }

    function loadNotice(clsName, name) {
        clsName = clsName || 'cloud-download';
        name = name || 'Loading';

        var span = $('<span class="oi oi-cloud-download" title="icon name" aria-hidden="true"></span>').attr('title', name).addClass('oi-' + clsName);

        $('#loadZone').append(span);

        return span;
    }

    function populateSelect(select, items) {
        var i = 0;
        select.empty();
        for (i = 0; i < items.length; i++) {
            select.append($('<option></option>').text(items[i]).attr('value', items[i]));
        }
    }

    var toaster = $('#toast');

    function resultHandler(result, action, value) {
        var status = true;
        if (!result) {
            status = false;
        } else if (result.status && result.status == 'ok') {
            info(result.msg);
        } else {
            error(result.msg);
            status = false;
        }
        if (status && result.msg == '' && action == 'renameScreen' && value) {
            $("option:selected", $('#edit-screens')).text(value);
        }
        return status;
    }

    function info(message) {
        if (message)
            toaster.text(message).css('background-color', '#ddffc7').fadeIn().delay(2000).fadeOut();
    }

    function error(message) {
        if (message)
            toaster.text(message).css('background-color', '#ffcac7').fadeIn().delay(2000).fadeOut();
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

    $('#update-logs').click(function(){
        statusCheck(false);
    });

    var lastSampleCount = -1;

    function statusCheck(firstTime) {

        if (firstTime) {
            lastSampleCount = -1;
        }

        var loadIcon = loadNotice(undefined, 'Status Check');

        $.getJSON({
            url: '/piper/status',
            data: {},
            complete: function(){
                loadIcon.remove();
            },
            success: function(data){

                var i = 0, item, div;

                configList.hide();

                switch( data.status  ) {
                    case 'READY': {
                        statusName.removeClass().addClass('navbar-brand').addClass('oi').addClass('oi-cog').text('');
                    } break;
                    case 'RUNNING': {
                        statusName.removeClass().addClass('navbar-brand').addClass('oi').addClass('oi-cog').addClass('rotate').text('');
                    } break;
                    case 'STOPPING':
                    case 'STOPPED': {
                        statusName.removeClass().addClass('navbar-brand').addClass('oi').addClass('oi-power-standby').text('');
                    } break;
                    default: {
                        statusName.removeClass().addClass('navbar-brand').text(data.status);
                    } break;
                }

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
                        //loadedForm.hide();
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
                            //loadedForm.show();
                            states.prop('disabled', false);
                            notWhileRunning.prop('disabled', false);
                            bodyElement.removeClass('running');
                        }
                    } break;
                    case 'RUNNING':

                        if ((data.stats.image_samples - 0) != lastSampleCount) {
                            lastSampleCount = data.stats.image_samples - 0;
                            $('#liveCanvas').css("height", ((256.0 / viewSetup.controlWidth) * viewSetup.controlHeight) + 'px').attr('src', '/piper/screen?factor=20&live=true&time=' + (new Date().getTime()));
                        }

                        $('#time_avg').text(data.stats.image_avg || '?');
                        $('#time_last').text(data.stats.image_last || '?');
                        $('#time_samples').text(data.stats.image_samples || '?');

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
                            //loadedForm.show();
                            states.prop('disabled', true);
                            notWhileRunning.prop('disabled', true);
                            bodyElement.addClass('running');
                            setTimeout(statusCheck, 3000);
                        }
                    } break;
                }

                if (data.state) {
                    states.val(data.state);
                }

                item = undefined;

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

                if (item && item.message) {
                    $('#last_log').text(item.message);
                }

                while (logItems.length > 75) {
                    logItems.splice(0,1)[0].detach();
                }

                if (data.variables.length > 0) {
                    for (i = 0; i < data.variables.length; i++) {
                        item = data.variables[i];
                        if (linkedVariables[item.name] && !linkedVariables[item.name].is(":focus"))
                            linkedVariables[item.name].val(formatVariable(item));
                    }
                }
            }
        });
    }

    $(".logging").change(function(){
        var ref = $(this), level = ref.val(), mode = ref.attr('mode');
        var loadIcon = loadNotice(undefined, 'Log Change: ' + mode + " - " + level);
        $.ajax({
            type: "POST",
            url: '/piper/process/level/' + mode + '/' + level,
            complete: function(){
            	loadIcon.remove();
            },
            success: function(result){

            }
        });
    });

    function playPauseFunc() {
        var loadIcon = loadNotice(undefined, 'Play/Pause');
        if (!playPauseButton.hasClass('disabled')) {
            $.ajax({
              type: "POST",
              url: '/piper/process/playPause/' + states.val(),
              complete: function(){
                loadIcon.remove();
              },
              success: function(result){
                statusCheck();
              }
            });
        }
    }

    statusName.click(function(){
        if (statusName.hasClass('oi-cog')) {
            playPauseFunc();
        }
    });

    playPauseButton.click(function(){
        playPauseFunc();
    });

    intentButton.click(function(){
        var loadIcon = loadNotice(undefined, 'Intent to Pause');
        if (!intentButton.hasClass('disabled')) {
            $.ajax({
              type: "POST",
              url: '/piper/process/intent/pause',
              complete: function(){
                loadIcon.remove();
              },
              success: function(result){
                statusCheck();
              }
            });
        }
    });

    unloadButton.click(function(){
        var loadIcon = loadNotice(undefined, 'Unload App');

        if (!unloadButton.hasClass('disabled')) {
            $.ajax({
              type: "POST",
              url: '/piper/process/unload',
              complete: function(){
                loadIcon.remove();
              },
              success: function(result){
                statusCheck();
                $('[href="#home"]').tab('show');
              }
            });
        }
    });

    killButton.click(function(){
        var loadIcon = loadNotice(undefined, 'Kill App');
        if (!unloadButton.hasClass('disabled')) {
            $.ajax({
                type: "POST",
                url: '/piper/process/kill',
                complete: function(){
                    loadIcon.remove();
                },
                success: function(result){
                    statusCheck();
                    $('[href="#home"]').tab('show');
                }
            });
        }
    });

    dumpStateButton.click(function(){
        var loadIcon = loadNotice(undefined, 'Dump State');
        $.ajax({
            type: "POST",
            url: '/piper/dump/state',
            dataType: 'json',
            complete: function(){
            	loadIcon.remove();
            },
            success: function(result){
                console.log(result);
            }
        });
    });

    unloadEdit.click(function(){
        var loadIcon = loadNotice(undefined, 'Unload Edit');
        if (!unloadEdit.hasClass('disabled')) {
            $.ajax({
              type: "POST",
              url: '/piper/edit/unload',
              complete: function(){
                loadIcon.remove();
              },
              success: function(result){
                statusCheck();
                $('[href="#home"]').tab('show');
              }
            });
        }
    });

    controlAdb.click(function(){
        var cmd = $(this).attr('adb'), loadIcon = loadNotice(undefined, 'Adb Command: ' + cmd);
        adbInfo.val("Please Wait...");
        $.ajax({
            type: "POST",
            url: '/piper/adb/' + cmd,
            data: {},
            complete: function(){
                loadIcon.remove();
            },
            success: function(result){
                adbInfo.val(result.value || 'Done');
            }
        });
    });

    $('.controlButton').click(function(){
        var ref= $(this), button = ref.attr('controlButton'), componentId = $('#components').val();
        var loadIcon = loadNotice(undefined, 'Control Action: ' + button + " - " + componentId);
        $.ajax({
            type: "POST",
            url: '/piper/control/component/' + encodeURIComponent(componentId) + '/' + encodeURIComponent(button),
            complete: function(){
                loadIcon.remove();
            }
        });
    });

    $('.eventButton').click(function(){
        var ref= $(this), button = ref.attr('controlButton');
        var loadIcon = loadNotice(undefined, 'Event Action: ' + button);
        $.ajax({
            type: "POST",
            url: '/piper/control/key/event/' + encodeURIComponent(button),
            complete: function(){
                loadIcon.remove();
            }
        });
    });

    configList.on('click', 'button', function(){
        var ref = $(this), index = ref.attr('index') - 0;
        if (index >= 0 && index < configs.length) {
            if (ref.hasClass('run-config')) {
                applySelection(configs[index]);
                loadButton.click();
            } else if (ref.hasClass('delete-config')) {
                deleteConfigurations(configs[index]);
            } else if (ref.hasClass('modify-config')) {
                applySelection(configs[index]);
                $('[href="#config"]').tab('show');
            }
        }
    });

    shortcutList.on('click', 'button', function(){
            var ref = $(this), value = ref.attr('state');
            states.val(value);
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
        var loadIcon = loadNotice(undefined, 'Load App');
        $.ajax({
          type: "GET",
          url: '/piper/process/info',
          complete: function(){
                    loadIcon.remove();
          },
          success: function(result){

            resultHandler(result);

            if (result.status == 'ok') {
                states.empty();
                components.empty();

                viewSetup.viewWidth = result.viewWidth;
                viewSetup.viewHeight = result.viewHeight;
                viewSetup.controlWidth = result.controlWidth;
                viewSetup.controlHeight = result.controlHeight;

                var i, valueLoop, item, grp, label, input, button, def;
                for (i = 0; i < result.states.length; i++) {
                states.append($('<option></option>').attr('value', result.states[i].value).text(result.states[i].name).attr('description', result.states[i].description));
                }
                buildShortcuts(result.states);
                for (i = 0; i < result.components.length; i++) {
                components.append($('<option></option>').attr('value', result.components[i].value).text(result.components[i].name));
                }
                $('#consoleLogging').val(result.consoleLevel);
                $('#webLogging').val(result.webLevel);
                $('#fileLogging').val(result.fileLevel);
                variableContainer.empty();
                linkedVariables = {};
                var varTiers = {}, varTab, tabRow, tabContainer, varNavLink;
                var varTabs = {};
                var myTabContent = $('#myTabContent');
                // Remove all existing links
                var dropdownVarLinks = $('#dropdownVarLinks').empty();
                // Remove all custom variable tabs
                $('.var-container').remove();
                // Create the variable tabs
                if (result.variableTabs && result.variableTabs.length > 0) {
                    for (i = 0; i < result.variableTabs.length; i++) {
                        varTab = $('<div class="tab-pane fade var-container var-tier-tab" role="tabpanel" aria-labelledby="vars-tab"></div>').attr('id',"vt_"+result.variableTabs[i].id).attr('aria-labelledby',"vt_"+result.variableTabs[i].id+'-tab').appendTo(myTabContent);
                        var temp = $('<div class="export-all-container"></div>').appendTo(varTab);
                        $('<a href="#" class="export-all-link">Export Tab</a>').appendTo(temp);
                        tabRow = $('<div class="row"></div>').appendTo(varTab);
                        tabContainer = $("<div class=\"container-fluid\"></div>").appendTo(tabRow);
                        varTabs[result.variableTabs[i].id] = tabContainer;
                        // Create the click link
                        varNavLink = $('<a class="nav-link" data-toggle="tab" role="tab" aria-selected="false"></a>')
                            .attr('aria-controls', "vt_"+result.variableTabs[i].id)
                            .attr('href', '#' + "vt_"+result.variableTabs[i].id)
                            .attr('id', "vt_"+result.variableTabs[i].id+'-tab')
                            .text(result.variableTabs[i].title)
                            .appendTo(dropdownVarLinks);
                    }
                }
                // Create the default variable link
                $('<a class="nav-link" id="vars-tab" data-toggle="tab" href="#vars" role="tab" aria-controls="vars" aria-selected="false">Variables</a>').appendTo(dropdownVarLinks)
                // Setup the default tab
                varTabs['*'] = $('#variable-container');

                var tabTo;

                // Create the Variable Tiers
                if (result.variableTiers && result.variableTiers.length > 0) {
                    for (i = 0; i < result.variableTiers.length; i++) {
                        tabTo = varTabs[result.variableTiers[i].tabId || '*'];
                        if (!tabTo) { // default to the fallback tab
                            tabTo = varTabs['*'];
                        }
                        def = $('<div class="var-tier-item"></div>').appendTo(tabTo);
                        $('<a href="#" class="export-link">Export Tier</a>').appendTo(def);
                        $('<a href="#" class="reset-link">Reset Tier</a>').appendTo(def);
                        def.append($('<h3></h3>').text(result.variableTiers[i].title));

                        varTiers[result.variableTiers[i].id] = $('<div class="row"></div>').appendTo(def);
                    }
                }

                // Fallback tier
                def = $('<div></div>').appendTo(varTabs['*']);
                def.append($('<h3></h3>').text('General'));
                varTiers['*'] = $('<div class="row"></div>').appendTo(def);

                if (result.variables.length > 0) {
                    variableForm.show();

                    var row, gen;

                    for (i = 0; i < result.variables.length; i++) {
                        item = result.variables[i];

                        row = undefined;
                        if (item.tierId) {
                            row = varTiers[item.tierId];
                        }
                        if (!row) { // Default to the fallback tier
                            row = varTiers['*'];
                        }

                        grp = $('<div class="input-group col-sm-12 col-md-6 col-lg-4" style="margin-bottom: 1em"></div>')
                            .appendTo(row);

                        $('<div class="input-group-prepend"></div>')
                            .append($('<span class="input-group-text" id=""></span>')
                                .text(item.display).attr('title', item.description || ''))
                            .appendTo(grp);

                        if (item.values && item.values.length > 0) {
                            gen = $('<select class="form-control notWhileRunning updateVariable"></select>');
                            for (valueLoop = 0; valueLoop < item.values.length; valueLoop++) {
                                gen.append($('<option></option>').text(item.values[valueLoop].name).attr('value', item.values[valueLoop].value));
                            }
                            linkedVariables[item.name] = gen;
                        } else if (item.displayType == 'BOOLEAN') {
                           linkedVariables[item.name] = $('<select class="form-control notWhileRunning updateVariable"><option value="0">False</option><option value="1">True</option></select>')
                        } else {
                            linkedVariables[item.name] = $('<input type="text" class="form-control updateVariable"/>');
                            if (item.modify == 'EDITABLE') {
                                linkedVariables[item.name].addClass('notWhileRunning');
                            }
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
        var loadIcon = loadNotice(undefined, 'Load Editor');
        $.ajax({
          type: "GET",
          url: '/piper/edit/view/info',
          complete: function(){
            loadIcon.remove();
          },
          success: function(result){

            resultHandler(result);

            if (result.status == 'ok') {
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

    function updateScreenComponentNames() {
        var component_id = editComponents.val();
        $('#ComponentName').text('c-' + component_id + '.png');
        var screen_id = editScreens.val();
        $('#ScreenName').text('s-' + screen_id + '.png');
    }

    myTabContent.on('change', 'select.updateVariable, input.updateVariable', function(){
        var ref = $(this);
        ref.addClass('updatedValue');
    });

    myTabContent.on('click', 'button.updateVariable', function(){
        var ref = $(this), key = ref.data('key'), input = linkedVariables[key];
        var loadIcon = loadNotice(undefined, 'Update Variable: ' + key);
        input.removeClass('updatedValue');
        if (input.val()) {
            $.ajax({
              type: "POST",
              url: '/piper/variable',
              complete: function(){
              	loadIcon.remove();
              },
              data: {key: key, value: input.val()}
            });
        }
    });

    $('#myTabContent').on('click', '.export-link', function(){
        var parent = $(this).parent('.var-tier-item');
        var items = {};
        $('button.updateVariable', parent).each(function(){
            var ref = $(this), key = ref.data('key'), input = linkedVariables[key];
            items[key] = input.val();
        });
        $('#import-text-area').val(JSON.stringify(items));
        $('[href="#imports"]').tab('show');
    });

    $('#myTabContent').on('click', '.reset-link', function(){
        var parent = $(this).parent('.var-tier-item');
        var items = [];
        $('button.updateVariable', parent).each(function(){
            var ref = $(this), key = ref.data('key');
            items.push(key);
        });
        var loadIcon = loadNotice(undefined, 'Reset');
        $.ajax({
            type: "POST",
            url: '/piper/reset',
            data: {items: JSON.stringify(items)},
            complete: function(){
            	loadIcon.remove();
            },
            success: function(){
                // Reset red markings
                $('input.updatedValue,select.updatedValue').removeClass('updatedValue');
                statusCheck();
            }
        });WebRes

    });

    $('#myTabContent').on('click', '.export-all-link', function(){
            var parent = $(this).parents('.var-tier-tab');
            var items = {};
            $('button.updateVariable', parent).each(function(){
                var ref = $(this), key = ref.data('key'), input = linkedVariables[key];
                items[key] = input.val();
            });
            $('#import-text-area').val(JSON.stringify(items));
            $('[href="#imports"]').tab('show');
        });

    $('.import-action').click(function(){
        var loadIcon = loadNotice(undefined, 'Import');
        if (!$(this).hasClass('disabled')) {
            var content = $('#import-text-area').val();
            if (content) {
                $.ajax({
                  type: "POST",
                  url: '/piper/variables',
                  data: {content: content},
                  complete: function(){
                  	loadIcon.remove();
                  },
                  success: function(){
                        // Reset red markings
                        $('input.updatedValue,select.updatedValue').removeClass('updatedValue');
                        statusCheck();
                    }
                });
            }
        }
    });

    ///////////////////////////////////////////////////////////////////////////
    // EDIT
    ///////////////////////////////////////////////////////////////////////////

    $('#uploadScreenButton').click(function(){
        var ref = $(this), form = ref.parents('form.uploadBase');
        uploadCommon(ref, form, "SCREEN");
    });

    $('#uploadComponentButton').click(function(){
        var ref = $(this), form = ref.parents('form.uploadBase');
        uploadCommon(ref, form, "COMPONENT");
    });

    function uploadCommon(ref, form, action) {
        var list = $('#' + ref.attr('lst'));
        var id = list.val();
        form.attr('action', '/piper/edit/upload/' + action + "/" + id);
        form.submit();
    }

    $('.edit-action').click(function() {
        var ref = $(this), action = ref.attr('editvalue'), list, id, value = 'null', lstItem;
        if (ref.hasClass('prompt-name')) {
            id = $.trim(prompt('Name:'));
        } else {
            list = $('#' + ref.attr('lst'));
            id = list.val();
            if (ref.hasClass('prompt-value')) {
               value = $.trim(prompt('Value:', $("option:selected", list).text()));
            }
        }
        if (id) {
            var loadIcon = loadNotice(undefined, 'Edit Action: ' + action + ' - ' + id);
            $.ajax({
                type: "POST",
                url: '/piper/edit/action/' + action + "/" + id + '/' + encodeURIComponent(value),
                complete: function(){
                    loadIcon.remove();
                },
                success: function (result) {
                    resultHandler(result, action, value);
                },
                dataType: 'json'
            });
        } else {
            error("Please enter a ID");
        }
    });

    $('#extractScreen').click(function(){
        var ref = $(this), lst, id;
        list = $('#' + ref.attr('lst'));
        id = list.val();

        var loadIcon = loadNotice(undefined, 'Extract Screen: ' + id);
        if (id) {
            $.ajax({
                type: "POST",
                url: '/piper/edit/extract/' + id,
                complete: function(){
                    loadIcon.remove();
                },
                success: function (result) {
                    if (resultHandler(result) && result.content) {
                        $('#screenExtractArea').val(result.content);
                    }
                },
                dataType: 'json'
            });
        }
    });

    $('#importScreen').click(function(){
            var ref = $(this), content = $('#screenExtractArea').val();

            var loadIcon = loadNotice(undefined, 'Import Screen');
            if (content) {
                $.ajax({
                    type: "POST",
                    url: '/piper/edit/import/screen',
                    data: {
                        content: content
                    },
                    complete: function(){
                        loadIcon.remove();
                    },
                    success: function (result) {
                        resultHandler(result);
                    },
                    dataType: 'json'
                });
            }
        });


    ///////////////////////////////////////////////////////////////////////////
    // Configuration
    ///////////////////////////////////////////////////////////////////////////

    var loadButton = $('#controlLoad');
    var saveButton = $('#controlSave');
    var downloadConfigButton = $('#downloadConfig');
    var downloadStateButton = $('#downloadState');

    function createConfigItem(before, title, cssClazz, data, editLink) {
        var wrap = $('<div class="input-group extra-field"></div>');
        wrap.append($('<div class="input-group-prepend"></div>').append($('<span class="input-group-text">Title</span>').text(title)));
        var select = $('<select class="form-control"></select>').addClass(cssClazz).appendTo(wrap);
        populateSelect(select, data || []);
        var grp = $('<div class="input-group-append"></div>');
        grp.append($('<button type="button" class="btn btn-danger remove oi oi-trash"></button>').data('row', wrap));
        if (editLink) {
            grp.append($('<button type="button" class="btn btn-info edit-view oi oi-pencil"></button>').data('row', wrap));
        }
        wrap.append(grp);
        $('<br class="extra-field"/>').insertBefore(before);
        wrap.insertBefore(before);
        return select;
    }

    function resetConfigPage() {
        var configForm = $('#config');

        $('#configName').val('');

        populateSelect($('#configConfigName'), settings.configs || []);
        populateSelect($('#configStateName'), settings.states || []);

        $('#configConfigName').val('');
        $('#configStateName').val('');

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
        var configName = $('#configConfigName').val();
        var stateName = $('#configStateName').val();

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
            configName: configName || (scriptList && scriptList[0]) || '',
            stateName: stateName || (scriptList && scriptList[0]) || '',
            device: device1 || '',
            views: viewList,
            scripts: scriptList,
            title: title,
            attributes: attributes
        };

        return data;
    }

    function saveConfig(callback) {

        var data = extractConfig();

        if (!data.title) {
            error('Please provide a name');
            return;
        }

        var loadIcon = loadNotice(undefined, 'Save Config');

        $.ajax({
            type: "POST",
            url: '/piper/configs',
            data: JSON.stringify(data),
            headers: {
                'Content-Type': 'application/json'
            },
            complete: function(){
            	loadIcon.remove();
            },
            success: function(result){
                if (resultHandler(result)) {
                    loadConfigurations(callback);
                }
          },
          dataType: 'json'
        });
    }

    function downloadConfig() {
        $('#config-download-name').val($('#configConfigName').val());
        $('#config-download-form').submit();
    }

    function downloadState() {
        $('#state-download-name').val($('#configStateName').val());
        $('#state-download-form').submit();
    }

    function is_valid_name(name) {
        var re = /[0-9A-Za-z_-]+/;
        return name.match(re);
    }

    function applySelection(j) {

        resetConfigPage();

        $('#configName').val(j.title || '');

        $('#configConfigName').val(j.configName || '');
        $('#configStateName').val(j.stateName || '');

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
                    createConfigItem($('#addViewHolder'), 'View', 'view-item', settings.views, true).val(item);
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

    $('#addConfigButton').click(function(){
        var name = prompt("Config Name");
        if (name && is_valid_name(name)) {
            settings.configs.push(name);
            populateSelect($('#configConfigName'), settings.configs || []);
            $('#configConfigName').val(name);
        }
    });

    $('#addStateButton').click(function(){
        var name = prompt("Config Name");
        if (name && is_valid_name(name)) {
            settings.states.push(name);
            populateSelect($('#configStateName'), settings.states || []);
            $('#configStateName').val(name);
        }
    });

    $('#config').on('click', '.remove', function(){
        $(this).data('row').remove();
    });

    $('#config').on('click', '.edit-view', function(){

        var ref = $(this), blob = extractConfig();

        var viewName = ref.parents('div.input-group').find('select').val();

        if (!viewName) return;

        blob.views = [];

        blob.views.push(viewName);

        var loadIcon = loadNotice(undefined, 'Edit View');

        $.ajax({
          type: "POST",
          url: '/piper/edit/view',
          data: JSON.stringify(blob),
          headers: {
                'Content-Type': 'application/json'
            },
            complete: function(){
            	loadIcon.remove();
            },
          success: function(result){
            if (result.status == 'ok') {
                loadEditViewInfo(true);
                $('[href="#edit"]').tab('show');
            }
          },
          dataType: 'json'
        });

    });

    saveButton.click(function(){
        if (!saveButton.hasClass('disabled')) {
            saveConfig();
        }
    });

    downloadConfigButton.click(function(){
        downloadConfig();
    });

    downloadStateButton.click(function(){
        downloadState();
    });

    loadButton.click(function(){

        saveConfig(function(){
            var loadIcon = loadNotice(undefined, 'Load');

            var blob = extractConfig();
            $.ajax({
              type: "POST",
              url: '/piper/process/prep',
              data: JSON.stringify(blob),
              headers: {
                    'Content-Type': 'application/json'
                },
                complete: function(){
                    loadIcon.remove();
                },
              success: function(result){
                if (result.status == 'ok') {

                    viewSetup.viewWidth = result.viewWidth;
                    viewSetup.viewHeight = result.viewHeight;
                    viewSetup.controlWidth = result.controlWidth;
                    viewSetup.controlHeight = result.controlHeight;

                    loadProcessInfo(true);
                    $('[href="#run"]').tab('show');
                }
              },
              dataType: 'json'
            });
        });

    });

    ///////////////////////////////////////////////////////////////////////////
    // Control
    ///////////////////////////////////////////////////////////////////////////

    var controlImage = new Image();

    var isLoadingImage = false;
    var isImageLoaded = false;

    controlImage.addEventListener('load', function() {
        console.log("Image Loaded");
        isLoadingImage = false;

        var c = $('#controlCanvas');
        var ctx = c[0].getContext('2d');

        //ctx.drawImage(controlImage, 0, 0, viewSetup.viewWidth - 0, viewSetup.viewHeight - 0, 0, 0, canvasWidth - 0, canvasHeight - 0);
        ctx.drawImage(controlImage, 0, 0, canvasWidth - 0, canvasHeight - 0);

        isImageLoaded = true;
    }, false);

    controlImage.addEventListener('error', function() {
            console.log("Image Error");
            isLoadingImage = false;

            var c = $('#controlCanvas');
            var ctx = c[0].getContext('2d');

            ctx.fillStyle = "#FFFFFF";
            ctx.fillRect(0, 0, canvasWidth, 100);

            ctx.font = "30px Arial";
            ctx.fillText("Error", 50, 50);

            isImageLoaded = false;
    }, false);

    var determineImageWidth = -1;
    var determineImageHeight = -1;

    function updateControlPreview(cached, download) {
        canvasResize();
        requestControlPreviewUpdate(cached, download);
    }

    function requestControlPreviewUpdate(cached, download) {

        if (!isLoadingImage) {

            var c = $('#controlCanvas');
            c.prop('cached', cached ? 'C' : 'N');
            var ctx = c[0].getContext('2d');

            ctx.fillStyle = "#FFFFFF";
            ctx.fillRect(0, 0, canvasWidth, 100);

            ctx.fillStyle = "#000000";
            ctx.font = "30px Arial";
            ctx.fillText("Please Wait, Prepping...", 50, 50);

            var loadIcon = loadNotice(undefined, 'Screen Update');
            isLoadingImage = true;
            $.ajax({
                type: "POST",
                dataType:'json',
                url: '/piper/screen/prep' + (cached ? '/cache' : ''),
                complete: function(){
                    loadIcon.remove();
                    isLoadingImage = false;
                },
                success: function(result){
                    if (download) {
                        $('#downloadScreenForm').submit();
                    } else {

                        ctx.fillStyle = "#FFFFFF";
                        ctx.fillRect(0, 0, canvasWidth, 100);

                        ctx.fillStyle = "#000000";
                        ctx.font = "30px Arial";
                        ctx.fillText("Please Wait, Loading...", 50, 50);

                        updateControlPreviewImage();
                    }
                }
            });
        }

    }

    function updateControlPreviewImage() {
        controlImage.src = '/piper/screen?time=' + (new Date().getTime());
    }

    var canvasHolder = $('#canvasHolder');
    var controlCanvas = $('#controlCanvas');

    var canvasWidth = -1;
    var canvasHeight = -1;
    var canvasFactor = 1.0;

    $(window).resize(function() {
        canvasResize(true);
    });

    function canvasResize(repaint) {

         controlCanvas.hide();

        var iw = canvasHolder.innerWidth();
        if (iw <= 80) return;

        iw -= 60;

        if (viewSetup.controlWidth < iw) {
            canvasWidth = viewSetup.controlWidth - 0;
            canvasHeight = viewSetup.controlHeight - 0;
            canvasFactor = 1.0;
        } else {
            canvasFactor = viewSetup.controlWidth / iw;
            canvasWidth = iw;
            canvasHeight = Math.floor((iw * viewSetup.controlHeight) / viewSetup.controlWidth);
        }

        console.log({w:canvasWidth, h:canvasHeight});

        controlCanvas.attr('width', canvasWidth);
        controlCanvas.attr('height', canvasHeight);

        controlCanvas.css('width', canvasWidth + 'px');
        controlCanvas.css('height', canvasHeight + 'px');

        canvasHolder.css('min-height', canvasHeight + 'px');

        controlCanvas.show();

        if (repaint && isImageLoaded) {
            var ctx = controlCanvas[0].getContext('2d');
            //ctx.drawImage(controlImage, 0, 0, viewSetup.viewWidth - 0, viewSetup.viewHeight - 0, 0, 0, canvasWidth - 0, canvasHeight - 0);
            ctx.drawImage(controlImage, 0, 0, canvasWidth - 0, canvasHeight - 0);
        }
    }

    controlCanvas.click(function(e){
        var ref = $(this), mode = ref.prop('cached');
        if (mode != 'N') {
            return;
        }
        var points = getClickPosition(e);
        console.log(points);

        var c = controlCanvas;
        var ctx = c[0].getContext('2d');

        ctx.fillStyle = "#FFFFFF";
        ctx.fillRect(0, 0, canvasWidth, 100);

        ctx.fillStyle = "#000000";
        ctx.font = "30px Arial";
        ctx.fillText("Please Wait, Tapping...", 50, 50);

        var loadIcon = loadNotice(undefined, 'Tapping ' + points.x + " - " + points.y);

        $.ajax({
            type: "POST",
            dataType:'json',
            url: '/piper/control/tap/' + points.x + '/' + points.y,
            complete: function(){
                loadIcon.remove();
            },
            success: function(result){
                if (result && result.status == 'ok') {
                    setTimeout(function(){
                        requestControlPreviewUpdate(false, false);
                    }, 1000);
                }
            }
        });
    });

    function getClickPosition(e) {
        var parentPosition = getPosition(e.currentTarget);
        var xPosition = Math.floor((e.clientX - parentPosition.x) * canvasFactor);
        var yPosition = Math.floor((e.clientY - parentPosition.y) * canvasFactor);
        return {x: xPosition, y: yPosition};
    }

    function getPosition(el) {
      var xPos = 0;
      var yPos = 0;

      while (el) {
        if (el.tagName == "BODY") {
          // deal with browser quirks with body/window/document and page scroll
          var xScroll = el.scrollLeft || document.documentElement.scrollLeft;
          var yScroll = el.scrollTop || document.documentElement.scrollTop;

          xPos += (el.offsetLeft - xScroll + el.clientLeft);
          yPos += (el.offsetTop - yScroll + el.clientTop);
        } else {
          // for all other non-BODY elements
          xPos += (el.offsetLeft - el.scrollLeft + el.clientLeft);
          yPos += (el.offsetTop - el.scrollTop + el.clientTop);
        }

        el = el.offsetParent;
      }
      return {
        x: xPos,
        y: yPos
      };
    }

    $('#RefreshControl').click(function(){
        updateControlPreview(false);
    });

    $('#RefreshDownloadControl').click(function(){
        updateControlPreview(false, true);
    });

    $('#CacheRefreshControl').click(function(){
        updateControlPreview(true);
    });

    $('#CacheRefreshDownloadControl').click(function(){
        updateControlPreview(true, true);
    });

    ///////////////////////////////////////////////////////////////////////////
    // Shortcuts
    ///////////////////////////////////////////////////////////////////////////

    function buildShortcuts(states) {
        var i;
        shortcutList.empty();
        for (i = 0; i < states.length; i++) {
            createShortcutItem(i, states[i].name, states[i].description, states[i].value)
        }
    }

    function createShortcutItem(index, title, description, value) {
        if('!' === description) return;
        var wrap, card, body, text, bntGroup;
        wrap = $('<div class="col-sm-12 col-md-6 col-lg-4 col-xl-3 customConfig"></div>');
        card = $('<div class="card shadow-sm"></div>');
        $('<div class="card-header"></div>').append($('<h4 class="my-0 font-weight-normal"></h4>').text(title || 'Untitled')).appendTo(card);
        body = $('<div class="card-body"></div>').appendTo(card);
        $('<div class="card-text"></div>').text(description).appendTo(body);
        body.append($('<button type="button" class="btn btn-outline-dark">Select</button>').attr('state', value));
        wrap.append(card);
        shortcutList.append(wrap);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Home
    ///////////////////////////////////////////////////////////////////////////

    function buildConfigs() {
        var i;
        $('.customConfig',configList).remove();
        for (i = 0; i < configs.length; i++) {
            createHomeItem(i, configs[i].title, configs[i] && configs[i].scripts && configs[i].scripts.length > 0)
        }
    }

    function createHomeItem(index, title, allowRun) {
        var wrap, card, body, bntGroup;
        wrap = $('<div class="col-sm-12 col-md-6 col-lg-4 col-xl-3 customConfig"></div>');
        card = $('<div class="card shadow-sm"></div>');
        $('<div class="card-header"></div>').append($('<h4 class="my-0 font-weight-normal"></h4>').text(title || 'Untitled')).appendTo(card);
        body = $('<div class="card-body"></div>').appendTo(card);
        bntGroup = $('<div class="btn-group" role="group" aria-label="Controls"></div>').appendTo(body);
        if (allowRun) {
            bntGroup.append($('<button type="button" class="btn run-config btn-primary oi oi-media-play"></button>').attr('index', index));
        }
        bntGroup.append($('<button type="button" class="btn modify-config btn-outline-dark oi oi-cog"></button>').attr('index', index));
        bntGroup.append($('<button type="button" class="btn delete-config btn-outline-danger oi oi-trash"></button>').attr('index', index));
        wrap.append(card);
        configList.append(wrap);
    }

    function loadConfigurations(callback) {
        var loadIcon = loadNotice(undefined, 'Load Configurations');
        $.ajax({
          type: "GET",
          url: '/piper/configs',
          complete: function(){
              loadIcon.remove();
          },
          success: function(result){
            if (resultHandler(result)) {
                configs = result.configs;
                buildConfigs();
                if (callback) {
                    callback();
                }
            }
          },
          dataType: 'json'
        });
    }

    function deleteConfigurations(config) {

        var loadIcon = loadNotice(undefined, 'Delete Configuration');

        $.ajax({
          type: "DELETE",
          url: '/piper/configs/' + config.configName,
          complete: function(){
          	loadIcon.remove();
          },
          success: function(result){
            if (resultHandler(result)) {
                loadConfigurations();
            }
          },
          dataType: 'json'
        });
    }

    $('#new-config').click(function(){
        resetConfigPage();
        $('[href="#config"]').tab('show');
    });

    ///////////////////////////////////////////////////////////////////////////
    // First Time
    ///////////////////////////////////////////////////////////////////////////

    function firstTimeLoad() {
        var loadIcon = loadNotice(undefined, 'First Time Load');

        $.getJSON({
          url: '/piper/settings/list',
          data: {},
          complete: function(){
            loadIcon.remove();
          },
          success: function(data){
            settings = data;
            resetConfigPage();
            loadConfigurations();
            statusCheck(true);
          }
        });
    }

    function refeshStatesAndConfigs(){
        var loadIcon = loadNotice(undefined, 'Refresh Status And Configurations');

        $.getJSON({
          url: '/piper/settings/list',
          data: {},
          complete: function(){
          	loadIcon.remove();
          },
          success: function(data){
            //settings = data;
            //resetConfigPage();
            //loadConfigurations();
            //statusCheck(true);
          }
        });
    }

    setTimeout(firstTimeLoad, 100);
});