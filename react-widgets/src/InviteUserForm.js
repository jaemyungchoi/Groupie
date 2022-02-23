import React from 'react';
import axios from 'axios';
import { Button, Input, Item, Pagination, Message, Header, Segment, Divider, Form, Label, Icon, Dropdown } from 'semantic-ui-react'
import EventItem from './EventItem';
import { DateInput } from 'semantic-ui-calendar-react';
import moment from 'moment';

class InviteUserForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            candidates: [],
            prefix: null,
            partial: null,
            loading: false
        };
    }

    handleFocus = () => {
        let { prefix } = this.state;
        // load candidates the first time when dropdown gets focus
        if (prefix === null) {
            this.doSearch('');
        }
    }

    handleSearchChange = (e, { searchQuery }) => {
        let { prefix, partial } = this.state;
        // only load candidates if prefix is no longer valid
        // or the results are not complete
        if (!searchQuery.startsWith(prefix) || partial) {
            console.log('prefix', searchQuery);
            this.doSearch(searchQuery);
        }
    }

    handleSelect = (e, { value }) => {
        if (value === '') {
            return;
        }
        let { candidates } = this.state;
        if (candidates.find(candidate => candidate.value === value)?.disabled ?? true) {
            if (this.props.onError) {
                this.props.onError(`You're on ${value}'s block list.`);
            }
        } else {
            if (this.props.onSelect) {
                this.props.onSelect(value);
            }
        }
    }

    doSearch = (searchText) => {
        this.setState({ loading: true });
        axios.get('/api/usernames', {
            params: {
                prefix: searchText
            }
        })
            .then((response) => {
                console.log(response);
                let candidates = (response.data?.candidates ?? [])
                    .map(({ username, onTheirBlockList }) => ({
                        key: username,
                        value: username,
                        text: username,
                        disabled: onTheirBlockList,
                        description: onTheirBlockList ? 'unavailable' : null
                    }));
                this.setState({
                    loading: false,
                    candidates,
                    prefix: searchText,
                    partial: response.data?.partial ?? true
                });
                if (this.props.onError) {
                    this.props.onError(null);
                }
            })
            .catch((error) => {
                console.log(error);
                this.setState({ loading: false })
                if (this.props.onError) {
                    this.props.onError(String(error));
                }
            })
    }

    render() {
        let { candidates, loading } = this.state;
        return (
            <Form>
                <Form.Dropdown
                    id='invite-dropdown'
                    placeholder='Invite Users'
                    fluid
                    search
                    selection
                    clearable
                    options={candidates}
                    loading={loading}
                    selectOnBlur={false}
                    closeOnChange={false}
                    selectOnNavigation={false}
                    clearable
                    onSearchChange={this.handleSearchChange}
                    onFocus={this.handleFocus}
                    onChange={this.handleSelect}
                />
            </Form>
        );
    }
}

export default InviteUserForm;