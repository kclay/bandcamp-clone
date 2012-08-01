define(['libs/backbone/backbone.min'], function () {
    // Now that all the orignal source codes have ran and accessed each other
    // We can call noConflict() to remove them from the global name space
    // Require.js will keep a reference to them so we can use them in our modules
    //_.noConflict();
    //$.noConflict();


    (function (Backbone) {

        // The super method takes two parameters: a method name
        // and an array of arguments to pass to the overridden method.
        // This is to optimize for the common case of passing 'arguments'.
        function _super(methodName, args) {

            // Keep track of how far up the prototype chain we have traversed,
            // in order to handle nested calls to _super.
            this._superCallObjects || (this._superCallObjects = {});
            var currentObject = this._superCallObjects[methodName] || this,
                parentObject = findSuper(methodName, currentObject);
            this._superCallObjects[methodName] = parentObject;

            var result = parentObject[methodName].apply(this, args || []);
            delete this._superCallObjects[methodName];
            return result;
        }

        // Find the next object up the prototype chain that has a
        // different implementation of the method.
        function findSuper(methodName, childObject) {
            var object = childObject;
            while (object[methodName] === childObject[methodName]) {
                object = object.constructor.__super__;
            }
            return object;
        }

        _.each(["Model", "Collection", "View", "Router"], function (klass) {
            Backbone[klass].prototype._super = _super;

            Backbone[klass].prototype.debug = function () {
                if (window.console && window.console.log) {
                    window.console.log(arguments);
                }
            }
            Backbone[klass].prototype.log = function () {
                if (window.console && window.console.log) {
                    window.console.log(arguments);
                }
            }
            Backbone[klass].prototype.is = function (state, value) {
                var states = this.__states || (this.__states = {});
                if (typeof value != "undefined") {
                    states[state] = value;
                } else {
                    return states[value];
                }
            }
        });
        Backbone.View.prototype.delay = function (func, context, delay) {
            setTimeout(function () {
                func.call(context)
            }, delay || 100);
        }

    })(Backbone);
    init_backbone();
    return Backbone;

});
function init_backbone() {

    window.UpdatingCollectionView = Backbone.View.extend({
        attachMethod:"append",
        initialize:function (options) {
            _(this).bindAll('add', 'remove');

            if (!options.childViewConstructor) throw "no child view constructor provided";
            if (!options.childViewTagName) throw "no child view tag name provided";
            this._onRemoveCallback = options.onRemoveCallback;

            this._childViewConstructor = options.childViewConstructor;
            this._childViewTagName = options.childViewTagName;

            this._childViews = [];

            this.collection.each(this.add);

            this.collection.bind('add', this.add);
            this.collection.bind('remove', this.remove);
        },

        add:function (model) {
            model.collection = this.collection;
            var childView = this._createChildView(model, this._childViews.length);
            this._childViews.push(childView);

            if (this._rendered) {
                this._attach(childView);
                if (childView.init)childView.init();

                childView.delegateEvents();
            }

        },
        _createChildView:function (model,index) {
            return  new this._childViewConstructor({
                tagName:this._childViewTagName,
                model:model
            });

        },
        _attach:function (views) {
            var html = [];
            views = _.isArray(views) ? views : [views];
            _(views).each(function (childView) {
                html.push(childView.render().el);


            });
            this.$el.append(html);

        },

        remove:function (model) {
            var viewToRemove = _(this._childViews).select(function (cv) {
                return cv.model === model;
            })[0];
            this._childViews = _(this._childViews).without(viewToRemove);

            if (this._rendered) {
                var next = function () {

                    viewToRemove.undelegateEvents();
                    $(viewToRemove.el).remove();
                }
                if (this._onRemoveCallback) {
                    this._onRemoveCallback(viewToRemove, next)
                } else {

                    next();
                }


            }

        },

        render:function () {
            var that = this;
            this._rendered = true;

            this.empty();


            this._attach(this._childViews);
            _(this._childViews).each(function (childView) {
                childView.delegateEvents();


            });

            return this;
        }
    });
}
function enhanceBackbone(Backbone) {
    // Backbone.Validation v0.5.2
//
// Copyright (C)2011-2012 Thomas Pedersen
// Distributed under MIT License
//
// Documentation and full license available at:
// http://thedersen.github.com/backbone.validation

    Backbone.Validation = (function (Backbone, _, undefined) {
        var defaultOptions = {
            forceUpdate:false,
            selector:'name'
        };

        var getValidatedAttrs = function (model) {
            return _.reduce(_.keys(model.validation), function (memo, key) {
                memo[key] = undefined;
                return memo;
            }, {});
        };

        var getValidators = function (model, validation, attr) {
            var attrValidation = validation[attr] || {};

            if (_.isFunction(attrValidation)) {
                return attrValidation;
            } else if (_.isString(attrValidation)) {
                return model[attrValidation];
            } else if (!_.isArray(attrValidation)) {
                attrValidation = [attrValidation];
            }

            return _.reduce(attrValidation, function (memo, attrValidation) {
                _.each(_.without(_.keys(attrValidation), 'msg'), function (validator) {
                    memo.push({
                        fn:Backbone.Validation.validators[validator],
                        val:attrValidation[validator],
                        msg:attrValidation.msg
                    });
                });
                return memo;
            }, []);
        };

        var hasChildValidaton = function (validation, attr) {
            return _.isObject(validation) && _.isObject(validation[attr]) && _.isObject(validation[attr].validation);
        };

        var validateAttr = function (model, validation, attr, value, computed) {
            var validators = getValidators(model, validation, attr);

            if (_.isFunction(validators)) {
                return validators.call(model, value, attr, computed);
            }

            return _.reduce(validators, function (memo, validator) {
                var result = validator.fn.call(Backbone.Validation.validators, value, attr, validator.val, model, computed);
                if (result === false || memo === false) {
                    return false;
                }
                if (result && !memo) {
                    return validator.msg || result;
                }
                return memo;
            }, '');
        };

        var validateAll = function (model, validation, attrs, computed, view, options) {
            if (!attrs) {
                return false;
            }
            var isValid = true, error;
            for (var validatedAttr in validation) {
                error = validateAttr(model, validation, validatedAttr, model.get(validatedAttr), computed);
                if (_.isUndefined(attrs[validatedAttr]) && error) {
                    isValid = false;
                    break;
                } else if (!error && view) {
                    options.valid(view, validatedAttr, options.selector);
                }
                if (error !== false && hasChildValidaton(validation, validatedAttr)) {
                    isValid = validateAll(model, validation[validatedAttr].validation, attrs[validatedAttr], computed);
                }
            }
            return isValid;
        };

        var validateObject = function (view, model, validation, attrs, options, attrPath) {
            attrPath = attrPath || '';
            var result, error, changedAttr,
                errorMessages = [],
                invalidAttrs = [],
                isValid = true,
                computed = _.extend(model.toJSON(), attrs);

            for (changedAttr in attrs) {
                error = validateAttr(model, validation, changedAttr, attrs[changedAttr], computed);
                if (error) {
                    errorMessages.push(error);
                    invalidAttrs.push(attrPath + changedAttr);
                    isValid = false;
                    if (view) options.invalid(view, changedAttr, error, options.selector);
                } else {
                    if (view) options.valid(view, changedAttr, options.selector);
                }

                if (error !== false && hasChildValidaton(validation, changedAttr)) {

                    result = validateObject(view, model, validation[changedAttr].validation, attrs[changedAttr], options, attrPath + changedAttr + '.');

                    Array.prototype.push.apply(errorMessages, result.errorMessages);
                    Array.prototype.push.apply(invalidAttrs, result.invalidAttrs);
                    isValid = isValid && result.isValid;
                }
            }

            if (isValid) {
                isValid = validateAll(model, validation, attrs, computed, view, options);
            }

            return {
                errorMessages:errorMessages,
                invalidAttrs:invalidAttrs,
                isValid:isValid
            };
        };

        var mixin = function (view, options) {
            return {
                isValid:function (option) {
                    if (_.isString(option)) {
                        return !validateAttr(this, this.validation, option, this.get(option), this.toJSON());
                    }
                    if (_.isArray(option)) {
                        for (var i = 0; i < option.length; i++) {
                            if (validateAttr(this, this.validation, option[i], this.get(option[i]), this.toJSON())) {
                                return false;
                            }
                        }
                        return true;
                    }
                    if (option === true) {
                        this.validate();
                    }
                    return this.validation ? this._isValid : true;
                },
                validate:function (attrs, setOptions) {
                    var model = this,
                        opt = _.extend({}, options, setOptions);
                    if (!attrs) {
                        return model.validate.call(model, _.extend(getValidatedAttrs(model), model.toJSON()));
                    }

                    var result = validateObject(view, model, model.validation, attrs, opt);
                    model._isValid = result.isValid;

                    _.defer(function () {
                        model.trigger('validated', model._isValid, model, result.invalidAttrs);
                        model.trigger('validated:' + (model._isValid ? 'valid' : 'invalid'), model, result.invalidAttrs);
                    });

                    if (!opt.forceUpdate && result.errorMessages.length > 0) {
                        return result.errorMessages;
                    }
                }
            };
        };

        var bindModel = function (view, model, options) {
            _.extend(model, mixin(view, options));
        };

        var unbindModel = function (model) {
            delete model.validate;
            delete model.isValid;
        };

        var collectonAdd = function (model) {
            bindModel(this.view, model, this.options);
        };

        var collectionRemove = function (model) {
            unbindModel(model);
        };

        return {
            version:'0.5.2',

            configure:function (options) {
                _.extend(defaultOptions, options);
            },

            bind:function (view, options) {
                var model = view.model,
                    collection = view.collection,
                    opt = _.extend({}, defaultOptions, Backbone.Validation.callbacks, options);

                if (model) {
                    bindModel(view, model, opt);
                }
                if (collection) {
                    collection.each(function (model) {
                        bindModel(view, model, opt);
                    });
                    collection.bind('add', collectonAdd, {view:view, options:opt});
                    collection.bind('remove', collectionRemove);
                }
            },

            unbind:function (view) {
                var model = view.model,
                    collection = view.collection;
                if (model) {
                    unbindModel(view.model);
                }
                if (collection) {
                    collection.each(function (model) {
                        unbindModel(model);
                    });
                    collection.unbind('add', collectonAdd);
                    collection.unbind('remove', collectionRemove);
                }
            },

            mixin:mixin(null, defaultOptions)
        };
    }(Backbone, _));

    Backbone.Validation.callbacks = {
        valid:function (view, attr, selector) {
            view.$('[' + selector + '~=' + attr + ']')
                .removeClass('invalid')
                .removeAttr('data-error');
        },

        invalid:function (view, attr, error, selector) {
            view.$('[' + selector + '~=' + attr + ']')
                .addClass('invalid')
                .attr('data-error', error);
        }
    };

    Backbone.Validation.patterns = {
        digits:/^\d+$/,
        number:/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/,
        email:/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))$/i,
        url:/^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i
    };

    Backbone.Validation.messages = {
        required:'{0} is required',
        acceptance:'{0} must be accepted',
        min:'{0} must be grater than or equal to {1}',
        max:'{0} must be less than or equal to {1}',
        range:'{0} must be between {1} and {2}',
        length:'{0} must be {1} characters',
        minLength:'{0} must be at least {1} characters',
        maxLength:'{0} must be at most {1} characters',
        rangeLength:'{0} must be between {1} and {2} characters',
        oneOf:'{0} must be one of: {1}',
        equalTo:'{0} must be the same as {1}',
        pattern:'{0} must be a valid {1}',
        object:'{0} must be an object'
    };

    Backbone.Validation.validators = (function (patterns, messages, _) {
        var trim = String.prototype.trim ?
            function (text) {
                return text === null ? '' : String.prototype.trim.call(text);
            } :
            function (text) {
                var trimLeft = /^\s+/,
                    trimRight = /\s+$/;

                return text === null ? '' : text.toString().replace(trimLeft, '').replace(trimRight, '');
            };
        var format = function () {
            var args = Array.prototype.slice.call(arguments);
            var text = args.shift();
            return text.replace(/\{(\d+)\}/g, function (match, number) {
                return typeof args[number] != 'undefined' ? args[number] : match;
            });
        };
        var isNumber = function (value) {
            return _.isNumber(value) || (_.isString(value) && value.match(patterns.number));
        };
        var hasValue = function (value) {
            return !(_.isNull(value) || _.isUndefined(value) || (_.isString(value) && trim(value) === ''));
        };

        return {
            fn:function (value, attr, fn, model, computed) {
                if (_.isString(fn)) {
                    fn = model[fn];
                }
                return fn.call(model, value, attr, computed);
            },
            required:function (value, attr, required, model) {
                var isRequired = _.isFunction(required) ? required.call(model) : required;
                if (!isRequired && !hasValue(value)) {
                    return false; // overrides all other validators
                }
                if (isRequired && !hasValue(value)) {
                    return format(messages.required, attr);
                }
            },
            acceptance:function (value, attr) {
                if (value !== 'true' && (!_.isBoolean(value) || value === false)) {
                    return format(messages.acceptance, attr);
                }
            },
            min:function (value, attr, minValue) {
                if (!isNumber(value) || value < minValue) {
                    return format(messages.min, attr, minValue);
                }
            },
            max:function (value, attr, maxValue) {
                if (!isNumber(value) || value > maxValue) {
                    return format(messages.max, attr, maxValue);
                }
            },
            range:function (value, attr, range) {
                if (!isNumber(value) || value < range[0] || value > range[1]) {
                    return format(messages.range, attr, range[0], range[1]);
                }
            },
            length:function (value, attr, length) {
                if (!hasValue(value) || trim(value).length !== length) {
                    return format(messages.length, attr, length);
                }
            },
            minLength:function (value, attr, minLength) {
                if (!hasValue(value) || trim(value).length < minLength) {
                    return format(messages.minLength, attr, minLength);
                }
            },
            maxLength:function (value, attr, maxLength) {
                if (!hasValue(value) || trim(value).length > maxLength) {
                    return format(messages.maxLength, attr, maxLength);
                }
            },
            rangeLength:function (value, attr, range) {
                if (!hasValue(value) || trim(value).length < range[0] || trim(value).length > range[1]) {
                    return format(messages.rangeLength, attr, range[0], range[1]);
                }
            },
            oneOf:function (value, attr, values) {
                if (!_.include(values, value)) {
                    return format(messages.oneOf, attr, values.join(', '));
                }
            },
            equalTo:function (value, attr, equalTo, model, computed) {
                if (value !== computed[equalTo]) {
                    return format(messages.equalTo, attr, equalTo);
                }
            },
            pattern:function (value, attr, pattern) {
                if (!hasValue(value) || !value.toString().match(patterns[pattern] || pattern)) {
                    return format(messages.pattern, attr, pattern);
                }
            },
            validation:function (value, attr, objectValue) {
                if (!_.isObject(value)) {
                    return format(messages.object, attr);
                }
            }
        };
    }(Backbone.Validation.patterns, Backbone.Validation.messages, _));
}